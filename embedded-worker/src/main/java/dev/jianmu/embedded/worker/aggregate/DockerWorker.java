package dev.jianmu.embedded.worker.aggregate;

import java.io.BufferedWriter;

/**
 * @class DockerWorkerClient
 * @description DockerWorkerClient接口
 * @author Ethan Liu
 * @create 2021-04-14 18:45
*/
public interface DockerWorker {
    void createVolume(String volumeName);

    void deleteVolume(String volumeName);

    void runTask(DockerTask dockerTask, BufferedWriter logWriter);

    void resumeTask(DockerTask dockerTask, BufferedWriter logWriter);

    void terminateTask(String taskInstanceId);

    void deleteImage(String imageName);

    void updateImage(String imageName);
}
