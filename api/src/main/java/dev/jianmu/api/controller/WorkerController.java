package dev.jianmu.api.controller;

import dev.jianmu.api.dto.TaskInstanceAcceptingDto;
import dev.jianmu.api.dto.TaskInstanceUpdatingDto;
import dev.jianmu.api.dto.TaskInstanceWritingLogDto;
import dev.jianmu.api.dto.WorkerJoiningDto;
import dev.jianmu.api.vo.VolumeVo;
import dev.jianmu.api.vo.WorkerTaskVo;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.application.service.internal.WorkerInternalApplication;
import dev.jianmu.infrastructure.worker.DeferredResultService;
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

    public WorkerController(WorkerInternalApplication workerApplication, DeferredResultService deferredResultService, NodeDefApi nodeDefApi) {
        this.workerApplication = workerApplication;
        this.deferredResultService = deferredResultService;
        this.nodeDefApi = nodeDefApi;
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
        this.workerApplication.join(workerId, dto.getType(), dto.getName());
    }

    @GetMapping("{workerId}/ping")
    @Operation(summary = "ping Server接口", description = "ping Server接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void ping(@PathVariable("workerId") String workerId) {
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
                                .taskInstanceId(taskInstance.getId())
                                .volume(VolumeVo.builder()
                                        .name(taskInstance.getTriggerId())
                                        .type(taskInstance.isCreationVolume() ? VolumeVo.Type.CREATION : VolumeVo.Type.DELETION)
                                        .build())
                                .version(taskInstance.getVersion())
                                .build()
                        ));
            } else {
                deferredResult.setResult(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(WorkerTaskVo.builder()
                                .type(WorkerTaskVo.Type.TASK)
                                .taskInstanceId(taskInstance.getId())
                                .pullStrategy(null)
                                .containerSpec(this.workerApplication.getContainerSpec(taskInstance))
                                .resultFile(this.nodeDefApi.findByType(taskInstance.getDefKey()).getResultFile())
                                .version(taskInstance.getVersion())
                                .build()
                        ));
            }
        });
        return deferredResult;
    }

    @PostMapping("{workerId}/tasks/{taskInstanceId}")
    @Operation(summary = "获取终止任务接口", description = "获取终止任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public DeferredResult<ResponseEntity<?>> watchTasks(@PathVariable String workerId, @PathVariable("taskInstanceId") String taskInstanceId) {
        return this.deferredResultService.newWatchDeferredResult(workerId, taskInstanceId);
    }

    @PatchMapping("{workerId}/tasks/{taskInstanceId}/accept")
    @Operation(summary = "确定任务接口", description = "确定任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public WorkerTaskVo acceptTask(HttpServletResponse response, @PathVariable("workerId") String workerId,
                                   @PathVariable("taskInstanceId") String taskInstanceId, @Valid @RequestBody TaskInstanceAcceptingDto dto) {
        var taskInstance = this.workerApplication.acceptTask(response, workerId, taskInstanceId, dto.getVersion());
        if (response.getStatus() != HttpStatus.OK.value()) {
            return WorkerTaskVo.builder()
                    .taskInstanceId(taskInstanceId)
                    .build();
        }
        if (taskInstance.isVolume()) {
            return WorkerTaskVo.builder()
                    .type(WorkerTaskVo.Type.VOLUME)
                    .taskInstanceId(taskInstance.getId())
                    .volume(VolumeVo.builder()
                            .name(taskInstance.getTriggerId())
                            .type(taskInstance.isCreationVolume() ? VolumeVo.Type.CREATION : VolumeVo.Type.DELETION)
                            .build())
                    .version(taskInstance.getVersion() + 1)
                    .build();
        } else {
            return WorkerTaskVo.builder()
                    .type(WorkerTaskVo.Type.TASK)
                    .taskInstanceId(taskInstance.getId())
                    .pullStrategy(null)
                    .containerSpec(this.workerApplication.getContainerSpec(taskInstance))
                    .resultFile(this.nodeDefApi.findByType(taskInstance.getDefKey()).getResultFile())
                    .version(taskInstance.getVersion() + 1)
                    .build();
        }
    }

    @PatchMapping("{workerId}/tasks/{taskInstanceId}")
    @Operation(summary = "更新任务接口", description = "更新任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void updateTaskInstance(@PathVariable("workerId") String workerId, @PathVariable("taskInstanceId") String taskInstanceId, @Valid @RequestBody TaskInstanceUpdatingDto dto) {
        this.workerApplication.updateTaskInstance(workerId, taskInstanceId, dto.getStatus().name(), dto.getResultFile(), dto.getErrorMsg(), dto.getExitCode());
    }

    @PostMapping("{workerId}/tasks/{taskInstanceId}/logs")
    @Operation(summary = "写入任务日志接口", description = "写入任务日志接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void writeTaskLog(HttpServletRequest request, @PathVariable("workerId") String workerId, @PathVariable("taskInstanceId") String taskInstanceId) {
        var stringBuilder = new StringBuilder();
        try {
            var reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            if (!"null".equals(stringBuilder.toString())) {
                var list = TaskInstanceWritingLogDto.parseString(stringBuilder.toString());
                list.forEach(dto -> this.workerApplication.writeTaskLog(workerId, taskInstanceId, dto.getContent(), dto.getNumber(), dto.getTimestamp()));
            }
        } catch (IOException e) {
            throw new RuntimeException("任务日志写入失败： " + e);
        }
    }
}
