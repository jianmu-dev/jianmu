package dev.jianmu.infrastructure.kubernetes;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ethan Liu
 * @class PodWatcher
 * @description PodWatcher
 * @create 2022-01-23 22:15
 */
@Data
@Slf4j
public class PodWatcher {
    public enum State {
        INIT,
        STARTED,
        DONE
    }

    private String podName;
    private State state = State.INIT;
    private final Map<String, TaskWatcher> watcherMap = new ConcurrentHashMap<>();

    public PodWatcher(String podName) {
        this.podName = podName;
    }

    public void addTaskWatcher(TaskWatcher taskWatcher) {
        this.watcherMap.put(taskWatcher.getTaskInstanceId(), taskWatcher);
    }

    public void updateContainers(List<ContainerInfo> containerInfos) {
        containerInfos.forEach(this::updateContainer);
    }

    public void updateContainer(ContainerInfo containerInfo) {
        log.info("container id is: {} image is: {}", containerInfo.getId(), containerInfo.getImage());
        var container = this.watcherMap.get(containerInfo.getId());
        if (container == null) {
            log.info("unknown container id: {}", containerInfo.getId());
            return;
        }
        // 容器已完成或失败
        if (container.isFailed()) {
            if (containerInfo.getState() != ContainerInfo.State.TERMINATED) {
                log.info("Container zombie found... container: {}", containerInfo);
            }
            container.containerStateChange();
            return;
        }
        // placeholder容器执行中，k8s下载真正的镜像中
        if (container.isPlaceholder(containerInfo.getImage()) && container.isRunning() && containerInfo.getRestartCount() == 0) {
            log.info("placeholder容器执行中，k8s下载真正的镜像中");
            return;
        }
        if (containerInfo.getExitCode() == 2 && containerInfo.getReason().equals("Error")) {
            if (containerInfo.getRestartCount() == 0) {
                log.info("k8s可能后台下载真正的镜像中");
                return;
            }
        }
        switch (containerInfo.getState()) {
            case TERMINATED:
                if (container.isPlaceholder(containerInfo.getImage())) {
                    container.setState(TaskWatcher.State.PLACEHOLDER_FAILED);
                } else {
                    container.setState(TaskWatcher.State.FINISHED);
                }
                container.setExitCode(containerInfo.getExitCode());
                container.setReason(containerInfo.getReason());
                container.containerStateChange();
                return;
            case WAITING:
            case RUNNING:
                this.updateState(container, containerInfo);
                return;
            default:
                log.warn("非法状态");
        }
    }

    private void updateState(TaskWatcher container, ContainerInfo containerInfo) {
        TaskWatcher.State s;
        if (container.isPlaceholder(containerInfo.getImage()) ||
                containerInfo.getState() == ContainerInfo.State.WAITING) {
            s = TaskWatcher.State.WAITING;
        } else {
            s = TaskWatcher.State.RUNNING;
        }
        // state unchanged
        if (s == container.getState() && container.getReason().equals(containerInfo.getReason())) {
            return;
        }

        container.setState(s);
        container.setExitCode(0);
        container.setReason(containerInfo.getReason());
        container.containerStateChange();
    }
}
