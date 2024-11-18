package dev.jianmu.api.controller;

import dev.jianmu.api.vo.WorkerVo;
import dev.jianmu.application.service.WorkerApplication;
import dev.jianmu.application.service.internal.WorkerInternalApplication;
import dev.jianmu.worker.aggregate.Worker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("frontend/workers")
@Tag(name = "Worker API", description = "为前端提供的API")
public class WorkerController {

    private final WorkerApplication workerApplication;

    public WorkerController(WorkerApplication workerApplication) {
        this.workerApplication = workerApplication;
    }

    @GetMapping("/types")
    @Operation(summary = "Worker类型获取接口", description = "Worker类型获取接口")
    public Worker.Type[] getTypes() {
        return Worker.Type.values();
    }

    @DeleteMapping("{workerId}")
    @Operation(summary = "Worker删除接口", description = "Worker删除接口")
    public void delete(@PathVariable("workerId") String workerId) {
        this.workerApplication.delete(workerId);
    }

    @GetMapping("")
    @Operation(summary = "查询Worker列表", description = "查询Worker列表")
    public List<WorkerVo> findWorkerList() {
        var workers = this.workerApplication.findAll();
        return workers.stream().map(worker -> WorkerVo.builder()
                        .id(worker.getId())
                        .name(worker.getName())
                        .type(worker.getType())
                        .tags(worker.getTags())
                        .os(worker.getOs())
                        .arch(worker.getArch())
                        .capacity(worker.getCapacity())
                        .createdTime(worker.getCreatedTime())
                        .build())
                .collect(Collectors.toList());
    }
}
