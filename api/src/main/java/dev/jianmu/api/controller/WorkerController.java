package dev.jianmu.api.controller;

import dev.jianmu.api.dto.TaskInstanceAcceptingDto;
import dev.jianmu.api.dto.TaskInstanceUpdatingDto;
import dev.jianmu.api.dto.TaskInstanceWritingLogDto;
import dev.jianmu.api.dto.WorkerJoiningDto;
import dev.jianmu.api.vo.WorkerJoinVo;
import dev.jianmu.api.vo.WorkerTaskVo;
import dev.jianmu.worker.aggregate.Worker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

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
    public WorkerJoinVo join(@PathVariable("workerId") String workerId, @Valid @RequestBody WorkerJoiningDto dto) {
        return null;
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
    public WorkerTaskVo pullTasks(@PathVariable String workerId) {
        return null;
    }

    @PatchMapping("{workerId}/tasks/{taskInstanceId}/accept")
    @Operation(summary = "确定任务接口", description = "确定任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void acceptTask(@PathVariable("workerId") String workerId, @PathVariable("taskInstanceId") String taskInstanceId, @Valid @RequestBody TaskInstanceAcceptingDto dto) {

    }

    @PatchMapping("{workerId}/tasks/{taskInstanceId}")
    @Operation(summary = "更新任务接口", description = "更新任务接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void updateTaskInstance(@PathVariable("workerId") String workerId, @PathVariable("taskInstanceId") String taskInstanceId, @Valid @RequestBody TaskInstanceUpdatingDto dto) {

    }

    @PostMapping("{workerId}/tasks/{taskInstanceId}/logs")
    @Operation(summary = "写入任务日志接口", description = "写入任务日志接口")
    @Parameters({
            @Parameter(name = "X-Jianmu-Token", in = ParameterIn.HEADER, description = "认证token")
    })
    public void writeTaskLog(@PathVariable("workerId") String workerId, @PathVariable("taskInstanceId") String taskInstanceId, @Valid @RequestBody TaskInstanceWritingLogDto dto) {

    }
}
