package dev.jianmu.api.controller;

import dev.jianmu.api.dto.TaskInstanceAcceptingDto;
import dev.jianmu.api.dto.TaskInstanceUpdatingDto;
import dev.jianmu.api.dto.TaskInstanceWritingLogDto;
import dev.jianmu.api.dto.WorkerJoiningDto;
import dev.jianmu.api.vo.WorkerTaskVo;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.application.service.internal.WorkerInternalApplication;
import dev.jianmu.infrastructure.docker.ContainerSpec;
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

import javax.validation.Valid;

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
//        this.workerApplication.join(workerId, dto.getType(), dto.getName());
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
        var deferredResult = new DeferredResult<ResponseEntity<?>>(1000L * 10, null);
        deferredResult.setResult(ResponseEntity
                .status(HttpStatus.OK)
                .body(WorkerTaskVo.builder()
                        .type(WorkerTaskVo.Type.TASK)
                        .taskInstanceId("1")
                        .pullStrategy(null)
                        .containerSpec(ContainerSpec.builder()
                                .image("alpine:3.13.6")
                                .working_dir("triggerId")
                                .environment(new String[]{"JM_SHARE_DIR=triggerId"})
                                .entrypoint(new String[]{"sh"})
                                .args(new String[]{"echo 1"})
                                .build())
                        .resultFile("usr/local/result")
                        .version(0)
                        .build()
                ));
//        this.workerApplication.pullTasks(workerId).ifPresent(taskInstance -> {
//            if (taskInstance.getDefKey().equals("start") || taskInstance.getDefKey().equals("end")) {
//                deferredResult.setResult(ResponseEntity
//                        .status(HttpStatus.OK)
//                        .body(WorkerTaskVo.builder()
//                                .type(WorkerTaskVo.Type.VOLUME)
//                                .taskInstanceId(taskInstance.getId())
//                                .volume(VolumeVo.builder()
//                                        .name(taskInstance.getTriggerId())
//                                        .type(taskInstance.getDefKey().equals("start") ? VolumeVo.Type.CREATION : VolumeVo.Type.DELETION)
//                                        .build())
//                                .version(taskInstance.getVersion())
//                                .build()
//                        ));
//            } else {
//                deferredResult.setResult(ResponseEntity
//                        .status(HttpStatus.OK)
//                        .body(WorkerTaskVo.builder()
//                                .type(WorkerTaskVo.Type.TASK)
//                                .taskInstanceId(taskInstance.getId())
//                                .pullStrategy(null)
//                                .containerSpec(this.workerApplication.getContainerSpec(taskInstance))
//                                .resultFile(this.nodeDefApi.findByType(taskInstance.getDefKey()).getResultFile())
//                                .version(taskInstance.getVersion())
//                                .build()
//                        ));
//            }
//        });
        return deferredResult;
    }

    @PatchMapping("{workerId}/tasks/{taskInstanceId}/accept")
    @Operation(summary = "确定任务接口", description = "确定任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void acceptTask(@PathVariable("workerId") String workerId, @PathVariable("taskInstanceId") String taskInstanceId, @Valid @RequestBody TaskInstanceAcceptingDto dto) {
//        this.workerApplication.acceptTask(workerId, taskInstanceId, dto.getVersion());
    }

    @PatchMapping("{workerId}/tasks/{taskInstanceId}")
    @Operation(summary = "更新任务接口", description = "更新任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void updateTaskInstance(@PathVariable("workerId") String workerId, @PathVariable("taskInstanceId") String taskInstanceId, @Valid @RequestBody TaskInstanceUpdatingDto dto) {
//        this.workerApplication.updateTaskInstance(workerId, taskInstanceId, dto.getStatus(), dto.getResultFile(), dto.getErrorMsg(), dto.getExitCode());
    }

    @PostMapping("{workerId}/tasks/{taskInstanceId}/logs")
    @Operation(summary = "写入任务日志接口", description = "写入任务日志接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void writeTaskLog(@PathVariable("workerId") String workerId, @PathVariable("taskInstanceId") String taskInstanceId, @Valid @RequestBody TaskInstanceWritingLogDto dto) {
//        this.workerApplication.writeTaskLog(workerId, taskInstanceId, dto.getContent(), dto.getNumber(), dto.getTimestamp());
    }
}
