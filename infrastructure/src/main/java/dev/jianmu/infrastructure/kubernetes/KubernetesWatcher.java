package dev.jianmu.infrastructure.kubernetes;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.CallGeneratorParams;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class KubernetesWatcher
 * @description KubernetesWatcher
 * @create 2022-01-23 17:22
 */
@Slf4j
public class KubernetesWatcher {
    public final String defaultNamespace;
    private final SharedInformerFactory factory = new SharedInformerFactory();
    private final Map<String, PodWatcher> podWatcherMap = new ConcurrentHashMap<>();
    private Resource kubeConfigPath;

    public KubernetesWatcher(String namespace) {
        this.defaultNamespace = namespace;
        try {
            this.connect();
        } catch (IOException e) {
            log.warn("error: {}", e.getMessage());
            throw new RuntimeException("无法连接到Kubernetes集群");
        }
    }

    public KubernetesWatcher(Resource kubeConfigPath, String namespace) {
        this.defaultNamespace = namespace;
        this.kubeConfigPath = kubeConfigPath;
        try {
            this.connectWithConfig();
        } catch (IOException e) {
            log.warn("error: {}", e.getMessage());
            throw new RuntimeException("无法连接到Kubernetes集群");
        }
    }

    private void connect() throws IOException {
        ApiClient client = ClientBuilder.defaultClient();
        // 设置超时时间为0,即无限，如果watch的时间大于此时间会报socket timeout异常
        // infinite timeout
        OkHttpClient httpClient =
                client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);
        Configuration.setDefaultApiClient(client);
    }

    private void connectWithConfig() throws IOException {
        Assert.notNull(this.kubeConfigPath, "kubeConfigPath must not be null");
        Assert.isTrue(this.kubeConfigPath.exists(),
                () -> String.format("Resource %s does not exist", this.kubeConfigPath));
        var c = new FileReader(this.kubeConfigPath.getFile());
        ApiClient client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(c))
                .setReadTimeout(Duration.ZERO)
                .build();
        Configuration.setDefaultApiClient(client);
    }

    public void watch() {
        CoreV1Api api = new CoreV1Api();
        var podInformer = this.factory.sharedIndexInformerFor(
                (CallGeneratorParams params) -> api.listNamespacedPodCall(
                        defaultNamespace,
                        "true",
                        null,
                        null,
                        null,
                        null,
                        null,
                        params.resourceVersion,
                        null,
                        params.timeoutSeconds,
                        params.watch,
                        null),
                V1Pod.class,
                V1PodList.class
        );
        podInformer.addEventHandler(
                new ResourceEventHandler<>() {
                    @Override
                    public void onAdd(V1Pod v1Pod) {
                        var podName = v1Pod.getMetadata().getName();
                        log.info("Pod: {} Created", podName);
                    }

                    @Override
                    public void onUpdate(V1Pod oldPod, V1Pod newPod) {
                        var podName = newPod.getMetadata().getName();
                        log.info("Pod: {} Update", podName);
                        var podWatcher = podWatcherMap.get(podName);
                        if (podWatcher != null) {
                            podWatcher.updateContainers(extractContainers(newPod));
                        } else {
                            log.info("unknown pod");
                        }
                    }

                    @Override
                    public void onDelete(V1Pod v1Pod, boolean b) {
                        var podName = v1Pod.getMetadata().getName();
                        log.info("Pod: {} Deleted", podName);
                    }
                }
        );
        this.factory.startAllRegisteredInformers();
    }

    private List<ContainerInfo> extractContainers(V1Pod v1Pod) {
        log.info("Pod Phase is: {}", v1Pod.getStatus().getPhase());
        if (v1Pod.getStatus().getContainerStatuses() == null) {
            return List.of();
        }
        return v1Pod.getStatus().getContainerStatuses().stream()
                .map(v1ContainerStatus -> {
                    if (v1ContainerStatus.getState().getTerminated() != null) {
                        return ContainerInfo.builder()
                                .id(v1ContainerStatus.getName())
                                .state(ContainerInfo.State.TERMINATED)
                                .image(v1ContainerStatus.getImage())
                                .exitCode(v1ContainerStatus.getState().getTerminated().getExitCode())
                                .reason(v1ContainerStatus.getState().getTerminated().getReason())
                                .restartCount(v1ContainerStatus.getRestartCount())
                                .ready(v1ContainerStatus.getReady())
                                .build();
                    }
                    if (v1ContainerStatus.getState().getRunning() != null) {
                        return ContainerInfo.builder()
                                .id(v1ContainerStatus.getName())
                                .state(ContainerInfo.State.RUNNING)
                                .image(v1ContainerStatus.getImage())
                                .reason("")
                                .restartCount(v1ContainerStatus.getRestartCount())
                                .ready(v1ContainerStatus.getReady())
                                .build();
                    }
                    if (v1ContainerStatus.getState().getWaiting() != null) {
                        return ContainerInfo.builder()
                                .id(v1ContainerStatus.getName())
                                .state(ContainerInfo.State.WAITING)
                                .image(v1ContainerStatus.getImage())
                                .reason(v1ContainerStatus.getState().getWaiting().getReason())
                                .restartCount(v1ContainerStatus.getRestartCount())
                                .ready(v1ContainerStatus.getReady())
                                .build();
                    }
                    // kubernetes doc explains that this situation should be treated as Waiting state
                    return ContainerInfo.builder()
                            .id(v1ContainerStatus.getName())
                            .state(ContainerInfo.State.WAITING)
                            .image(v1ContainerStatus.getImage())
                            .reason("")
                            .restartCount(v1ContainerStatus.getRestartCount())
                            .ready(v1ContainerStatus.getReady())
                            .build();
                }).collect(Collectors.toList());
    }

    public void stop() {
        this.factory.stopAllRegisteredInformers();
    }

    public void addPodWatcher(PodWatcher podWatcher) {
        this.podWatcherMap.put(podWatcher.getPodName(), podWatcher);
    }

    public PodWatcher getPodWatcher(String podName) {
        return this.podWatcherMap.get(podName);
    }

    public void deletePodWatcher(String podName) {
        this.podWatcherMap.remove(podName);
    }
}
