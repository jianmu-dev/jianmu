package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.DockerTask;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import dev.jianmu.task.aggregate.spec.HostConfig;
import dev.jianmu.task.aggregate.spec.Mount;
import dev.jianmu.task.aggregate.spec.MountType;

import java.util.List;
import java.util.Map;

/**
 * @class: WorkerDomainService
 * @description: Worker领域服务
 * @author: Ethan Liu
 * @create: 2021-04-14 17:10
 **/
public class WorkerDomainService {

    public DockerTask createDockerTask(DockerDefinition taskDefinition, TaskInstance taskInstance, Map<String, String> environmentMap) {
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
        var spec = taskDefinition.getSpec();
        var newSpec = ContainerSpec.Builder.aContainerSpec()
                .image(spec.getImage())
                .workingDir(workingDir)
                .hostConfig(hostConfig)
                .cmd(spec.getCmd())
                .entrypoint(spec.getEntrypoint())
                .env(env)
                .build();
        return DockerTask.Builder.aDockerTask()
                .taskInstanceId(taskInstance.getId())
                .businessId(taskInstance.getBusinessId())
                .triggerId(taskInstance.getTriggerId())
                .defKey(taskInstance.getDefKey())
                .spec(newSpec)
                .build();
    }
}
