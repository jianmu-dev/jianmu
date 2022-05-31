package dev.jianmu.infrastructure.storage;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedWriter;
import java.io.File;

/**
 * @author Ethan Liu
 * @class StorageService
 * @description 文件存储服务
 * @create 2021-04-05 20:37
 */
public interface StorageService {
    void init();

    BufferedWriter writeLog(String LogFileName);

    SseEmitter readLog(String logFileName, int size, boolean isTask);

    SseEmitter randomReadLog(String logFileName, Integer line, Integer size, boolean isTask);

    File logFile(String LogFileName);

    File workflowLogFile(String LogFileName);

    BufferedWriter writeWebhook(String webhookFileName);

    String readWebhook(String webhookFileName);
}
