package dev.jianmu.api.controller;

import dev.jianmu.infrastructure.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    private final StorageService storageService;

    @Inject
    public LogController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{logId}")
    @Operation(summary = "日志获取接口", description = "日志获取接口，返回日志流")
    public StreamingResponseBody logStream(@Parameter(description = "任务实例ID即为日志ID") @PathVariable String logId) {
        BufferedReader reader = storageService.readLog(logId);
        return (os -> {
            readAndWrite(reader, os);
        });
    }

    private void readAndWrite(final BufferedReader reader, OutputStream os) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            os.write((line + "\n").getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/new/{logId}")
    @Operation(summary = "日志获取接口", description = "日志获取接口,可以使用Range方式分段获取")
    public ResponseEntity<FileSystemResource> getLog(@PathVariable String logId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(new FileSystemResource(this.storageService.logFile(logId)));
    }
}
