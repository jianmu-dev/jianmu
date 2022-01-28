package dev.jianmu.embedded.worker.aggregate;

import java.io.BufferedWriter;

/**
 * @author Ethan Liu
 * @class KubernetesWorker
 * @description KubernetesWorker
 * @create 2022-01-24 14:31
 */
public interface KubernetesWorker {
    void createPod(String podName);

    void deletePod(String podName);

    void runTask(DockerTask dockerTask, BufferedWriter logWriter);

    void resumeTask(DockerTask dockerTask, BufferedWriter logWriter);

    void terminateTask(String taskInstanceId);
}
