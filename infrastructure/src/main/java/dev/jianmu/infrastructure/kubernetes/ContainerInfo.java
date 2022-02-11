package dev.jianmu.infrastructure.kubernetes;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Ethan Liu
 * @class ContainerInfo
 * @description ContainerInfo
 * @create 2022-01-27 20:47
 */
@Getter
@Builder
@ToString
public class ContainerInfo {
    public enum State {
        WAITING,
        RUNNING,
        TERMINATED
    }

    private final String id;
    private final State state;
    private final String image;
    private final int exitCode;
    private final String reason;
    private final int restartCount;
    private final boolean ready;
}
