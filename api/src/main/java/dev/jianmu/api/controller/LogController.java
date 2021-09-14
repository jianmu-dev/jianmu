package dev.jianmu.api.controller;

import dev.jianmu.infrastructure.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class: LogController
 * @description: log接口
 * @author: Ethan Liu
 * @create: 2021-04-09 08:50
 **/
@RestController
@RequestMapping("logs")
@Tag(name = "任务日志接口", description = "本接口返回Chunked流，前端代码需要支持才能实时读取文件")
@SecurityRequirement(name = "bearerAuth")
public class LogController {
    private final StorageService storageService;

    public LogController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/new/{logId}")
    @Operation(summary = "任务日志获取接口", description = "任务日志获取接口,可以使用Range方式分段获取")
    public ResponseEntity<FileSystemResource> getLog(@PathVariable String logId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(new FileSystemResource(this.storageService.logFile(logId)));
    }

    @GetMapping("/workflow/{logId}")
    @Operation(summary = "流程日志获取接口", description = "流程日志获取接口,可以使用Range方式分段获取")
    public ResponseEntity<FileSystemResource> getWorkflowLog(@PathVariable String logId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(new FileSystemResource(this.storageService.workflowLogFile(logId)));
    }
}
