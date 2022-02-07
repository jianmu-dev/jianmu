package dev.jianmu.infrastructure.kubernetes;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author Ethan Liu
 * @class ContainerWatchInfo
 * @description ContainerWatchInfo
 * @create 2022-01-30 09:47
 */
@Data
@ToString
public class ContainerWatchInfo {
    public enum State {
        WAITING,
        RUNNING,
        FINISHED,
        PLACEHOLDER_FAILED,
        FAILED
    }

    private String id;
    private String image;
    private String placeholder;
    private State state;

    private int exitCode;
    private String reason;

    private LocalDateTime addedAt;
    private LocalDateTime failedAt;

    public ContainerWatchInfo(String id, String placeholder) {
        this.id = id;
        this.placeholder = placeholder;
    }

    public boolean isRunning() {
        return this.state == State.RUNNING || this.state == State.WAITING;
    }

    public boolean isPlaceholder(String placeholder) {
        return this.placeholder.equalsIgnoreCase(placeholder);
    }
}
