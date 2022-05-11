package dev.jianmu.infrastructure.storage;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

/**
 * @class StorageService
 * @description 文件存储服务
 * @author Ethan Liu
 * @create 2021-04-05 20:37
*/
public interface StorageService {
    void init();

    BufferedWriter writeLog(String LogFileName);

    SseEmitter readLog(String logFileName);

    File logFile(String LogFileName);

    File workflowLogFile(String LogFileName);

    BufferedWriter writeWebhook(String webhookFileName);

    String readWebhook(String webhookFileName);
}
