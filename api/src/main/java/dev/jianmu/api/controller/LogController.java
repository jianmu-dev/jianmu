package dev.jianmu.api.controller;

import dev.jianmu.infrastructure.storage.StorageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @class: LogController
 * @description: log接口
 * @author: Ethan Liu
 * @create: 2021-04-09 08:50
 **/
@RestController
@RequestMapping("log")
public class LogController {
    private final StorageService storageService;

    @Inject
    public LogController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{logId}")
    public StreamingResponseBody logStream(@PathVariable String logId) {
        BufferedReader reader = storageService.readLog(logId);
        return (os -> {
            readAndWrite(reader, os);
        });
    }

    private void readAndWrite(final BufferedReader reader, OutputStream os) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            os.write(line.getBytes(StandardCharsets.UTF_8));
        }
    }
}
