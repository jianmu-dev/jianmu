package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.*;

import java.util.List;
import java.util.Map;

/**
 * @class: WorkerDomainService
 * @description: Worker领域服务
 * @author: Ethan Liu
 * @create: 2021-04-14 17:10
 **/
public class WorkerDomainService {

    public DockerTask createDockerTask(TaskInstance taskInstance, Map<String, String> environmentMap) {
        var workerParameters = taskInstance.getWorkerParameters();
        var env = environmentMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new);
        // 使用TriggerId作为工作目录名称与volume名称
        var workingDir = "/" + taskInstance.getTriggerId();
        var volumeName = taskInstance.getTriggerId();

        var mount = Mount.Builder.aMount()
                .type(MountType.VOLUME)
                .source(volumeName)
                .target(workingDir)
                .build();
        var hostConfig = HostConfig.Builder.aHostConfig().mounts(List.of(mount)).build();
        var spec = ContainerSpec.Builder.aContainerSpec()
                .image(workerParameters.get("image"))
                .workingDir(workingDir)
                .hostConfig(hostConfig)
                // TODO CMD实现
//                .cmd()
                .env(env)
                .build();
        return DockerTask.Builder.aDockerTask()
                .taskInstanceId(taskInstance.getId())
                .businessId(taskInstance.getBusinessId())
                .triggerId(taskInstance.getTriggerId())
                .defKey(taskInstance.getDefKey())
                .defVersion(taskInstance.getDefVersion())
                .spec(spec)
                .build();
    }
}
