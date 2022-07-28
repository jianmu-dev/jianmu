package dev.jianmu.api.controller;

import dev.jianmu.api.dto.*;
import dev.jianmu.api.vo.Auth;
import dev.jianmu.api.vo.VolumeVo;
import dev.jianmu.api.vo.WorkerTaskVo;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.internal.WorkerInternalApplication;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.infrastructure.worker.DeferredResultService;
import dev.jianmu.infrastructure.worker.unit.Unit;
import dev.jianmu.worker.aggregate.Worker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * @author Ethan Liu
 * @class WorkerController
 * @description Worker API
 * @create 2021-04-21 14:40
 */
@RestController
@RequestMapping("workers")
@Tag(name = "Worker API", description = "Worker API")
public class WorkerController {
    private final WorkerInternalApplication workerApplication;
    private final DeferredResultService deferredResultService;
    private final NodeDefApi nodeDefApi;
    private final StorageService storageService;
    private final TaskInstanceApplication taskInstanceApplication;
    private final GlobalProperties globalProperties;

    public WorkerController(WorkerInternalApplication workerApplication,
                            DeferredResultService deferredResultService,
                            NodeDefApi nodeDefApi,
                            StorageService storageService,
                            TaskInstanceApplication taskInstanceApplication,
                            GlobalProperties globalProperties
    ) {
        this.workerApplication = workerApplication;
        this.deferredResultService = deferredResultService;
        this.nodeDefApi = nodeDefApi;
        this.storageService = storageService;
        this.taskInstanceApplication = taskInstanceApplication;
        this.globalProperties = globalProperties;
    }

    @GetMapping("/types")
    @Operation(summary = "Worker类型获取接口", description = "Worker类型获取接口")
    public Worker.Type[] getTypes() {
        return Worker.Type.values();
    }

    @PutMapping("{workerId}/join")
    @Operation(summary = "连接Server接口", description = "连接Server接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void join(@PathVariable("workerId") String workerId, @RequestBody @Valid WorkerJoiningDto dto) {
        this.workerApplication.join(workerId, dto.getType(), dto.getName(), dto.getTag());
    }

    @GetMapping("{workerId}/ping")
    @Operation(summary = "ping Server接口", description = "ping Server接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void ping(@PathVariable("workerId") String workerId) {
    }

    @GetMapping("kubernetes/{workerId}/tasks")
    @Operation(summary = "拉取kube任务接口", description = "拉取kube任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public DeferredResult<ResponseEntity<?>> pullKubeTasks(@PathVariable String workerId, TaskPullingDto taskPullingDto) {
        var deferredResult = this.deferredResultService.newPullDeferredResult(workerId);
        this.workerApplication.pullKubeTasks(workerId, taskPullingDto.getTriggerId()).ifPresent(taskInstance -> {
            var unit = this.workerApplication.findUnit(taskInstance);
            deferredResult.setResult(ResponseEntity.status(HttpStatus.OK).body(unit));
        });
        return deferredResult;
    }

    @GetMapping("{workerId}/tasks")
    @Operation(summary = "拉取任务接口", description = "拉取任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public DeferredResult<ResponseEntity<?>> pullTasks(@PathVariable String workerId) {
        var deferredResult = this.deferredResultService.newPullDeferredResult(workerId);
        this.workerApplication.pullTasks(workerId).ifPresent(taskInstance -> {
            if (taskInstance.isVolume()) {
                deferredResult.setResult(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(WorkerTaskVo.builder()
                                .type(WorkerTaskVo.Type.VOLUME)
                                .taskInstanceId(taskInstance.getBusinessId())
                                .volume(VolumeVo.builder()
                                        .name(taskInstance.getTriggerId())
                                        .type(taskInstance.isCreationVolume() ? VolumeVo.Type.CREATION : VolumeVo.Type.DELETION)
                                        .build())
                                .auth(this.getTaskAuth())
                                .version(taskInstance.getVersion())
                                .build()
                        ));
            } else {
                deferredResult.setResult(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(WorkerTaskVo.builder()
                                .type(WorkerTaskVo.Type.TASK)
                                .taskInstanceId(taskInstance.getBusinessId())
                                .pullStrategy(null)
                                .containerSpec(this.workerApplication.getContainerSpec(taskInstance))
                                .resultFile(this.nodeDefApi.findByType(taskInstance.getDefKey()).getResultFile())
                                .auth(this.getTaskAuth())
                                .version(taskInstance.getVersion())
                                .build()
                        ));
            }
        });
        return deferredResult;
    }

    @GetMapping("{workerId}/tasks/{businessId}")
    @Operation(summary = "获取任务详情接口", description = "获取任务详情接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public WorkerTaskVo findTaskById(@PathVariable String workerId, @PathVariable("businessId") String businessId) {
        var taskInstance = this.taskInstanceApplication.findByBusinessIdAndMaxSerialNo(businessId)
                .orElseThrow(() -> new RuntimeException("未找到任务:" + businessId));
        if (taskInstance.isVolume()) {
            return WorkerTaskVo.builder()
                    .type(WorkerTaskVo.Type.VOLUME)
                    .taskInstanceId(taskInstance.getBusinessId())
                    .volume(VolumeVo.builder()
                            .name(taskInstance.getTriggerId())
                            .type(taskInstance.isCreationVolume() ? VolumeVo.Type.CREATION : VolumeVo.Type.DELETION)
                            .build())
                    .auth(this.getTaskAuth())
                    .version(taskInstance.getVersion())
                    .build();
        } else {
            return WorkerTaskVo.builder()
                    .type(WorkerTaskVo.Type.TASK)
                    .taskInstanceId(taskInstance.getBusinessId())
                    .pullStrategy(null)
                    .containerSpec(this.workerApplication.getContainerSpec(taskInstance))
                    .resultFile(this.nodeDefApi.findByType(taskInstance.getDefKey()).getResultFile())
                    .auth(this.getTaskAuth())
                    .version(taskInstance.getVersion())
                    .build();
        }
    }

    private Auth getTaskAuth() {
        var registry = globalProperties.getWorker().getRegistry();
        return Auth.builder()
                .address(registry.getAddress())
                .username(registry.getUsername())
                .password(registry.getPassword())
                .build();
    }

    @PostMapping("{workerId}/tasks/{businessId}")
    @Operation(summary = "获取终止任务接口", description = "获取终止任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public DeferredResult<ResponseEntity<?>> watchTasks(@PathVariable String workerId, @PathVariable("businessId") String businessId) {
        return this.deferredResultService.newWatchDeferredResult(workerId, businessId);
    }

    @PatchMapping("{workerId}/tasks/{businessId}/accept")
    @Operation(summary = "确定任务接口", description = "确定任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public WorkerTaskVo acceptTask(HttpServletResponse response, @PathVariable("workerId") String workerId,
                                   @PathVariable("businessId") String businessId, @Valid @RequestBody TaskInstanceAcceptingDto dto) {
        var taskInstance = this.workerApplication.acceptTask(response, workerId, businessId, dto.getVersion());
        if (response.getStatus() != HttpStatus.OK.value()) {
            return WorkerTaskVo.builder()
                    .taskInstanceId(businessId)
                    .build();
        }
        if (taskInstance.isVolume()) {
            return WorkerTaskVo.builder()
                    .type(WorkerTaskVo.Type.VOLUME)
                    .taskInstanceId(taskInstance.getBusinessId())
                    .volume(VolumeVo.builder()
                            .name(taskInstance.getTriggerId())
                            .type(taskInstance.isCreationVolume() ? VolumeVo.Type.CREATION : VolumeVo.Type.DELETION)
                            .build())
                    .auth(this.getTaskAuth())
                    .version(taskInstance.getVersion() + 1)
                    .build();
        } else {
            return WorkerTaskVo.builder()
                    .type(WorkerTaskVo.Type.TASK)
                    .taskInstanceId(taskInstance.getBusinessId())
                    .pullStrategy(null)
                    .containerSpec(this.workerApplication.getContainerSpec(taskInstance))
                    .resultFile(this.nodeDefApi.findByType(taskInstance.getDefKey()).getResultFile())
                    .auth(this.getTaskAuth())
                    .version(taskInstance.getVersion() + 1)
                    .build();
        }
    }

    @PatchMapping("{workerId}/tasks/{businessId}")
    @Operation(summary = "更新任务接口", description = "更新任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void updateTaskInstance(@PathVariable("workerId") String workerId, @PathVariable("businessId") String businessId, @Valid @RequestBody TaskInstanceUpdatingDto dto) {
        this.workerApplication.updateTaskInstance(workerId, businessId, dto.getStatus().name(), dto.getResultFile(), dto.getErrorMsg(), dto.getExitCode());
    }

    @PostMapping("{workerId}/tasks/{businessId}/logs")
    @Operation(summary = "写入任务日志接口", description = "写入任务日志接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void writeTaskLog(HttpServletRequest request, @PathVariable("workerId") String workerId, @PathVariable("businessId") String businessId) {
        var taskInstance = this.taskInstanceApplication.findByBusinessIdAndMaxSerialNo(businessId)
                .orElseThrow(() -> new RuntimeException("未找到任务实例, businessId：" + businessId));
        try (var writer = this.storageService.writeLog(taskInstance.getId(), false)) {
            var reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                var list = TaskInstanceWritingLogDto.parseString(line);
                list.stream()
                        .filter(dto -> dto.getContent() != null)
                        .forEach(dto -> this.workerApplication.writeTaskLog(writer, workerId, taskInstance.getId(), dto.getContent(), dto.getNumber(), dto.getTimestamp()));
            }
        } catch (IOException e) {
            throw new RuntimeException("任务日志写入失败： " + e);
        }
    }

    @PostMapping("{workerId}/tasks/{businessId}/logs/batch")
    @Operation(summary = "实时写入任务日志接口", description = "实时写入任务日志接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void batchWriteTaskLog(HttpServletRequest request, @PathVariable("workerId") String workerId, @PathVariable("businessId") String businessId) {
        var taskInstance = this.taskInstanceApplication.findByBusinessIdAndMaxSerialNo(businessId)
                .orElseThrow(() -> new RuntimeException("未找到任务实例, businessId：" + businessId));
        try (var writer = this.storageService.writeLog(taskInstance.getId(), true)) {
            var reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                if ("null".equals(line)) {
                    continue;
                }
                var list = TaskInstanceWritingLogDto.parseString(line);
                list.stream()
                        .filter(dto -> dto.getContent() != null)
                        .forEach(dto -> this.workerApplication.writeTaskLog(writer, workerId, taskInstance.getId(), dto.getContent(), dto.getNumber(), dto.getTimestamp()));
            }
        } catch (IOException e) {
            throw new RuntimeException("任务日志写入失败： " + e);
        }
    }

    @GetMapping("/kubernetes/{workerId}/tasks/{triggerId}")
    @Operation(summary = "获取k8s运行中任务", description = "获取k8s运行中任务")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public Unit findRunningTaskByTriggerId(@PathVariable("workerId") String workerId, @PathVariable("triggerId") String triggerId) {
        return this.workerApplication.findRunningTaskByTriggerId(workerId, triggerId);
    }
}
