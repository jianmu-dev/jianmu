package dev.jianmu.api.controller;

import dev.jianmu.task.aggregate.Worker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class: WorkerController
 * @description: Worker API
 * @author: Ethan Liu
 * @create: 2021-04-21 14:40
 **/
@RestController
@RequestMapping("workers")
@Tag(name = "Worker API", description = "Worker API")
@SecurityRequirement(name = "bearerAuth")
public class WorkerController {
    @GetMapping("/types")
    @Operation(summary = "Worker类型获取接口", description = "Worker类型获取接口")
    public Worker.Type[] getTypes() {
        return Worker.Type.values();
    }
}
