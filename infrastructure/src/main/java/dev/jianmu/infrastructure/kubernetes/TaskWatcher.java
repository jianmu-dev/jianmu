package dev.jianmu.infrastructure.kubernetes;

import dev.jianmu.infrastructure.docker.TaskFailedEvent;
import dev.jianmu.infrastructure.docker.TaskFinishedEvent;
import dev.jianmu.infrastructure.docker.TaskRunningEvent;
import io.kubernetes.client.openapi.ApiException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author Ethan Liu
 * @class TaskWatcher
 * @description TaskWatcher
 * @create 2022-02-03 22:41
 */
@Data
@Builder
@AllArgsConstructor
@ToString
@Slf4j
public class TaskWatcher {
    public enum State {
        WAITING,
        RUNNING,
        FINISHED,
        PLACEHOLDER_FAILED,
        FAILED
    }

    private String image;
    private String placeholder;
    private State state;

    private int exitCode;
    private String reason;

    private LocalDateTime addedAt;
    private LocalDateTime failedAt;

    private String triggerId;
    private String taskInstanceId;
    private String taskName;
    private BufferedWriter logWriter;

    private boolean hasResult;
    private String resultFile;

    private final KubernetesClient client;
    private final ApplicationEventPublisher publisher;

    public void containerStateChange() {
        switch (this.state) {
            case WAITING:
                return;
            case RUNNING:
                this.taskStart();
                return;
            case FINISHED:
                this.taskSucceed();
                return;
            case FAILED:
            case PLACEHOLDER_FAILED:
                this.taskFailed();
                return;
            default:
                log.warn("非法状态!");
        }
    }

    public void taskStart() {
        this.publisher.publishEvent(TaskRunningEvent.builder().taskId(taskInstanceId).build());
        log.info("Task start id: {} trigger id: {}", taskInstanceId, triggerId);
        this.fetchLog();
        this.copyResult();
    }

    public void taskSucceed() {
        this.copyResult();
        this.publisher.publishEvent(
                TaskFinishedEvent.builder()
                        .triggerId(triggerId)
                        .taskId(taskInstanceId)
                        .cmdStatusCode(exitCode)
                        .resultFile(resultFile)
                        .build()
        );
        log.info("Task succeed id: {} trigger id: {} exitCode: {} reason: {}", taskInstanceId, triggerId, exitCode, reason);
    }

    public void taskFailed() {
        this.publisher.publishEvent(TaskFailedEvent.builder()
                .triggerId(triggerId)
                .taskId(taskInstanceId)
                .errorMsg(reason)
                .build());
        log.info("Task failed id: {} trigger id: {} exitCode: {} reason: {}", taskInstanceId, triggerId, exitCode, reason);
    }

    private void fetchLog() {
        try {
            this.client.fetchLog(triggerId, taskName, logWriter);
        } catch (IOException | ApiException e) {
            log.warn("获取日志失败: {}", e.getMessage());
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(triggerId)
                    .taskId(taskInstanceId)
                    .errorMsg(reason)
                    .build());
        }
    }

    private void copyResult() {
        if (!hasResult) {
            log.info("Task id: {} 无需获取结果文件", taskInstanceId);
            return;
        }
        var resultPath = "/" + this.triggerId + "/" + this.taskName;
        log.info("copy file: " + resultPath + " from container....");
        try {
            this.resultFile = this.client.copyArchivedFromContainer(triggerId, "jianmu-keepalive", resultPath);
            log.info("result: {}", this.resultFile);
        } catch (IOException | ApiException e) {
            log.warn("获取结果文件失败: {}", e.getMessage());
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(triggerId)
                    .taskId(taskInstanceId)
                    .errorMsg("获取结果文件失败")
                    .build());
        }
        log.info("copy file:" + resultPath + " from container done");
    }

    public boolean isRunning() {
        return this.state == State.RUNNING || this.state == State.WAITING;
    }

    public boolean isFailed() {
        return this.state == State.FAILED || this.state == State.PLACEHOLDER_FAILED;
    }

    public boolean isPlaceholder(String placeholder) {
        return this.placeholder.equalsIgnoreCase(placeholder);
    }
}
