package dev.jianmu.infrastructure.kubernetes;

import dev.jianmu.embedded.worker.aggregate.EmbeddedWorker;
import dev.jianmu.embedded.worker.aggregate.EmbeddedWorkerTask;
import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.docker.TaskFailedEvent;
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

    private final ApplicationEventPublisher publisher;
    private final GlobalProperties properties;

    public EmbeddedKubeWorker(ApplicationEventPublisher publisher, GlobalProperties properties) {
        this.publisher = publisher;
        this.properties = properties;
        var kubeConfigPath = properties.getWorker().getK8s().getKubeConfigPath();
        var namespace = this.properties.getWorker().getK8s().getNamespace();
        if (kubeConfigPath == null) {
            this.client = new KubernetesClient(namespace);
            this.watcher = new KubernetesWatcher(namespace);
        } else {
            this.client = new KubernetesClient(kubeConfigPath, namespace);
            this.watcher = new KubernetesWatcher(kubeConfigPath, namespace);
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
                    .imagePullPolicy(this.properties.getWorker().getK8s().getImagePullPolicy())
                    .volumeMounts(List.of(new V1VolumeMount().name(sharedName).mountPath(sharedName)))
                    .addEnvFromItem(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name(sharedName + containerName)))
                    .image(this.properties.getWorker().getK8s().getPlaceholder());
        }
        var container = new V1Container()
                .name(containerName)
                .imagePullPolicy(this.properties.getWorker().getK8s().getImagePullPolicy())
                .workingDir(spec.getWorkingDir())
                .volumeMounts(List.of(new V1VolumeMount().name(sharedName).mountPath(sharedName)))
                .addEnvFromItem(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name(sharedName + containerName)))
                .image(this.properties.getWorker().getK8s().getPlaceholder());
        if (null != spec.getEnv()) {
            Arrays.stream(spec.getEnv()).map(s -> {
                var ss = s.split("=", 2);
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
                .name(podName + "-jianmu-keepalive")
                .imagePullPolicy(this.properties.getWorker().getK8s().getImagePullPolicy())
                .volumeMounts(List.of(new V1VolumeMount().name(podName).mountPath(podName)))
                .image(this.properties.getWorker().getK8s().getKeepalive());
        container.addCommandItem("/bin/sh");
        container.addCommandItem("-c");
        container.addArgsItem("tail -f /dev/null");
        return container;
    }

    private List<V1ConfigMap> initConfigMaps(String podName, Map<String, ContainerSpec> specMap) {
        return specMap.keySet().stream()
                .map(spec -> new V1ConfigMap().metadata(new V1ObjectMeta().namespace(this.client.defaultNamespace).name(podName + spec))
                        .data(Map.of("foo", "boo"))
                ).collect(Collectors.toList());
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
        var mapName = task.getTriggerId() + task.getTaskName();
        var map = Arrays.stream(task.getSpec().getEnv()).map(s -> {
            var ss = s.split("=", 2);
            return Map.entry(ss[0], ss[1]);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new V1ConfigMap()
                .metadata(
                        new V1ObjectMeta()
                                .namespace(properties.getWorker().getK8s().getNamespace())
                                .labels(Map.of("podName", task.getTriggerId()))
                                .name(mapName)
                )
                .data(map);
    }

    public void createPod(String podName, Map<String, ContainerSpec> specMap) {
        try {
            log.info("try to create Pod: {}", podName);
            var pod = initPod(podName, specMap);
            var configMaps = initConfigMaps(podName, specMap);
            for (var configMap : configMaps) {
                this.client.createConfigMap(configMap);
            }
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
            this.client.replaceConfigMap(embeddedWorkerTask.getTriggerId() + embeddedWorkerTask.getTaskName(), this.buildConfigMap(embeddedWorkerTask));
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
                    .placeholder(this.properties.getWorker().getK8s().getPlaceholder())
                    .logWriter(logWriter)
                    .hasResult(embeddedWorkerTask.getResultFile() != null)
                    .state(TaskWatcher.State.WAITING)
                    .client(this.client)
                    .publisher(this.publisher)
                    .build();
            podWatcher.addTaskWatcher(taskWatcher);

            this.client.updatePod(pod);
            log.info("update pod success");
        } catch (ApiException e) {
            log.warn("e: {}", e.getResponseBody());
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskId(embeddedWorkerTask.getTaskInstanceId())
                    .errorMsg(e.getResponseBody())
                    .build());
        }
    }

    @Override
    public void resumeTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter) {
        try {
            var pod = this.client.getPod(embeddedWorkerTask.getTriggerId());

            // Add Task Watcher
            var podWatcher = this.watcher.getPodWatcher(embeddedWorkerTask.getTriggerId());
            var taskWatcher = TaskWatcher.builder()
                    .taskInstanceId(embeddedWorkerTask.getTaskInstanceId())
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskName(embeddedWorkerTask.getTaskName())
                    .placeholder(this.properties.getWorker().getK8s().getPlaceholder())
                    .logWriter(logWriter)
                    .hasResult(embeddedWorkerTask.getResultFile() != null)
                    .state(TaskWatcher.State.WAITING)
                    .client(this.client)
                    .publisher(this.publisher)
                    .build();
            podWatcher.addTaskWatcher(taskWatcher);
        } catch (ApiException e) {
            log.warn("e: {}", e.getResponseBody());
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskId(embeddedWorkerTask.getTaskInstanceId())
                    .errorMsg(e.getResponseBody())
                    .build());
        }
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
