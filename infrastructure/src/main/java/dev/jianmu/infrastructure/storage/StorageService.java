package dev.jianmu.infrastructure.storage;

import dev.jianmu.infrastructure.storage.vo.LogVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

/**
 * @author Ethan Liu
 * @class StorageService
 * @description 文件存储服务
 * @create 2021-04-05 20:37
 */
public interface StorageService {
    void init();

    BufferedWriter writeLog(String LogFileName, boolean append);

    SseEmitter readLog(String logFileName, int size, boolean isTask);

    List<LogVo> randomReadLog(String logFileName, Integer line, Integer size, boolean isTask);

    File logFile(String LogFileName);

    File workflowLogFile(String LogFileName);

    BufferedWriter writeWebhook(String webhookFileName);

    String readWebhook(String webhookFileName);

    void deleteWorkflowLog(String triggerId);

    void deleteTaskLog(String taskId);

    void deleteWebhook(String webhookRequestId);
}
