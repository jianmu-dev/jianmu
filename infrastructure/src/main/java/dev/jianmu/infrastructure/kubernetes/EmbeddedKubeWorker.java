package dev.jianmu.infrastructure.kubernetes;

import dev.jianmu.embedded.worker.aggregate.EmbeddedWorker;
import dev.jianmu.embedded.worker.aggregate.EmbeddedWorkerTask;
import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;
import dev.jianmu.infrastructure.GlobalProperties;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class EmbeddedKubeWorker
 * @description EmbeddedKubeWorker
 * @create 2022-01-09 08:43
 */
@Slf4j
@ConditionalOnProperty(prefix = "jianmu.worker", name = "type", havingValue = "EMBEDDED_KUBE")
@Service
public class EmbeddedKubeWorker implements EmbeddedWorker {
    private final KubernetesClient client;
    private final KubernetesWatcher watcher;

    private final String placeholder = "drone/placeholder:latest";
    private final String imagePullPolicy = "Always"; // IfNotPresent, Always and Never

    private final ApplicationEventPublisher publisher;
    private final GlobalProperties properties;

    public EmbeddedKubeWorker(ApplicationEventPublisher publisher, GlobalProperties properties) {
        this.publisher = publisher;
        this.properties = properties;
        var kubeConfigPath = properties.getWorker().getK8s().getKubeConfigPath();
        if (kubeConfigPath == null) {
            this.client = new KubernetesClient();
            this.watcher = new KubernetesWatcher();
        } else {
            this.client = new KubernetesClient(kubeConfigPath);
            this.watcher = new KubernetesWatcher(kubeConfigPath);
        }
        this.watcher.watch();
    }

    @PreDestroy
    public void destroy() {
        log.info("watcher stopping......");
        this.watcher.stop();
        log.info("watcher stopped");
    }

    private V1Container initPlaceholder(
            String containerName,
            String sharedName,
            ContainerSpec spec
    ) {
        if (spec == null) {
            return new V1Container()
                    .name(containerName)
                    .imagePullPolicy(imagePullPolicy)
                    .volumeMounts(List.of(new V1VolumeMount().name(sharedName).mountPath(sharedName)))
                    .addEnvFromItem(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name(sharedName)))
                    .image(placeholder);
        }
        var container = new V1Container()
                .name(containerName)
                .imagePullPolicy(imagePullPolicy)
                .workingDir(spec.getWorkingDir())
                .volumeMounts(List.of(new V1VolumeMount().name(sharedName).mountPath(sharedName)))
                .addEnvFromItem(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name(sharedName)))
                .image(placeholder);
        if (null != spec.getEnv()) {
            Arrays.stream(spec.getEnv()).map(s -> {
                var ss = s.split("=");
                return new V1EnvVar().name(ss[0]).value(ss[1]);
            }).forEach(container::addEnvItem);
        }
        if (null != spec.getEntrypoint() && spec.getEntrypoint().length > 0) {
            log.info("Command is: {}", Arrays.asList(spec.getEntrypoint()));
            Arrays.stream(spec.getEntrypoint()).forEach(container::addCommandItem);
        }
        if (null != spec.getCmd() && spec.getCmd().length > 0) {
            Arrays.stream(spec.getCmd()).forEach(container::addArgsItem);
        }
        return container;
    }

    private V1Container buildKeepalive(String podName) {
        var container = new V1Container()
                .name("jianmu-keepalive")
                .imagePullPolicy(imagePullPolicy)
                .volumeMounts(List.of(new V1VolumeMount().name(podName).mountPath(podName)))
                .image("alpine:3.13.6");
        container.addCommandItem("/bin/sh");
        container.addCommandItem("-c");
        container.addArgsItem("tail -f /dev/null");
        return container;
    }

    private V1ConfigMap initConfigMap(String mapName) {
        return new V1ConfigMap()
                .metadata(new V1ObjectMeta().namespace("jianmu").name(mapName))
                .data(Map.of("foo", "boo"));
    }

    private V1Pod initPod(String podName, Map<String, ContainerSpec> specMap) {
        List<V1Container> containers = specMap.entrySet().stream().map(entry ->
                initPlaceholder(entry.getKey(), podName, entry.getValue())
        ).collect(Collectors.toList());
        // Add keepalive container
        containers.add(buildKeepalive(podName));

        V1ObjectMeta meta = new V1ObjectMeta()
                .name(podName)
                .namespace(null)
                .annotations(null)
                .labels(null);
        var spec = new V1PodSpec()
                .restartPolicy("Never")
                .serviceAccountName("")
                .volumes(List.of(new V1Volume().name(podName).emptyDir(new V1EmptyDirVolumeSource())))
                .containers(containers)
                .initContainers(null)
                .nodeName(null)
                .nodeSelector(null)
                .tolerations(null)
                .imagePullSecrets(null)
                .hostAliases(null)
                .dnsConfig(null);
        return new V1Pod()
                .spec(spec)
                .metadata(meta);
    }

    private V1ConfigMap buildConfigMap(EmbeddedWorkerTask task) {
        var mapName = task.getTriggerId();
        var map = Arrays.stream(task.getSpec().getEnv()).map(s -> {
            var ss = s.split("=");
            return Map.entry(ss[0], ss[1]);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new V1ConfigMap()
                .metadata(
                        new V1ObjectMeta()
                                .namespace(properties.getWorker().getK8s().getNamespace())
                                .name(mapName)
                )
                .data(map);
    }

    public void createPod(String podName, Map<String, ContainerSpec> specMap) {
        try {
            log.info("try to create Pod: {}", podName);
            var pod = initPod(podName, specMap);
            this.client.createConfigMap(initConfigMap(podName));
            this.client.createPod(pod);
            var podWatcher = new PodWatcher(podName);
            this.watcher.addPodWatcher(podWatcher);
        } catch (ApiException e) {
            log.warn("e: {}", e.getResponseBody());
            throw new RuntimeException("Pod创建失败");
        }
    }

    public void deletePod(String podName) {
        try {
            this.client.deletePod(podName);
            this.client.deleteConfigMap(podName);
            this.watcher.deletePodWatcher(podName);
        } catch (ApiException e) {
            log.warn("e: {}", e.getResponseBody());
        }
    }

    @Override
    public void createVolume(String volumeName, Map<String, ContainerSpec> specMap) {
        this.createPod(volumeName, specMap);
    }

    @Override
    public void deleteVolume(String volumeName) {
        this.deletePod(volumeName);
    }

    @Override
    public void runTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter) {
        try {
            log.info("update configMap");
            this.client.replaceConfigMap(embeddedWorkerTask.getTriggerId(), this.buildConfigMap(embeddedWorkerTask));
            var pod = this.client.getPod(embeddedWorkerTask.getTriggerId());
            var containers = pod.getSpec().getContainers();
            for (V1Container c : containers) {
                if (c.getName().equals(embeddedWorkerTask.getTaskName())) {
                    c.setImage(embeddedWorkerTask.getSpec().getImage());
                    log.info("pod {} container {} to image: {}", pod.getMetadata().getName(), c.getName(), c.getImage());
                }
            }
            // Add Task Watcher
            var podWatcher = this.watcher.getPodWatcher(embeddedWorkerTask.getTriggerId());
            var taskWatcher = TaskWatcher.builder()
                    .taskInstanceId(embeddedWorkerTask.getTaskInstanceId())
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskName(embeddedWorkerTask.getTaskName())
                    .placeholder(placeholder)
                    .logWriter(logWriter)
                    .state(TaskWatcher.State.WAITING)
                    .client(this.client)
                    .publisher(this.publisher)
                    .build();
            podWatcher.addTaskWatcher(taskWatcher);

            this.client.updatePod(pod);
            log.info("update pod success");
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resumeTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter) {

    }

    @Override
    public void terminateTask(String triggerId, String taskInstanceId) {
        this.deletePod(triggerId);
    }

    @Override
    public void deleteImage(String imageName) {

    }

    @Override
    public void updateImage(String imageName) {

    }
}
