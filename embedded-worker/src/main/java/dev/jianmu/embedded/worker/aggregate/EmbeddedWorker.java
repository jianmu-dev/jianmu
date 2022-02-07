package dev.jianmu.embedded.worker.aggregate;

import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;

import java.io.BufferedWriter;
import java.util.Map;

/**
 * @author Ethan Liu
 * @class DockerWorkerClient
 * @description DockerWorkerClient接口
 * @create 2021-04-14 18:45
 */
public interface EmbeddedWorker {
    void createVolume(String volumeName, Map<String, ContainerSpec> specMap);

    void deleteVolume(String volumeName);

    void runTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter);

    void resumeTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter);

    void terminateTask(String triggerId, String taskInstanceId);

    void deleteImage(String imageName);

    void updateImage(String imageName);
}
