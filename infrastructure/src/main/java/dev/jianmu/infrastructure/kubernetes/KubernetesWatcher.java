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

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

/**
 * @author Ethan Liu
 * @class KubernetesWatcher
 * @description KubernetesWatcher
 * @create 2022-01-23 17:22
 */
@Slf4j
public class KubernetesWatcher {
    private ApiClient client;
    public final String defaultNamespace;
    private final SharedInformerFactory factory = new SharedInformerFactory();

    public KubernetesWatcher() throws IOException {
        this.defaultNamespace = "jianmu";
        this.connect();
    }

    public KubernetesWatcher(String namespace) throws IOException {
        this.defaultNamespace = namespace;
        this.connect();
    }

    private void connect() throws IOException {
        // loading the in-cluster config, including:
        //   1. service-account CA
        //   2. service-account bearer-token
        //   3. service-account namespace
        //   4. master endpoints(ip, port) from pre-set environment variables
//        ApiClient client = ClientBuilder.cluster().build();

        // file path to your KubeConfig
        String kubeConfigPath = "/Users/ethan-liu/Documents/Java/jianmu-main/config";

        // loading the out-of-cluster config, a kubeconfig from file-system
        this.client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
                // 设置超时时间为10分钟，如果watch的时间大于此时间会报socket timeout异常
                .setReadTimeout(Duration.ZERO)
                .build();

        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);
    }

    public void watch(PodEventHandler eventHandler) {
        CoreV1Api api = new CoreV1Api();
        var podInformer = this.factory.sharedIndexInformerFor(
                (CallGeneratorParams params) -> {
                    return api.listNamespacedPodCall(
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
                            null);
                },
                V1Pod.class,
                V1PodList.class
        );
        podInformer.addEventHandler(
                new ResourceEventHandler<V1Pod>() {
                    @Override
                    public void onAdd(V1Pod v1Pod) {
                        log.info("Pod: {} Created", v1Pod.getMetadata().getName());
                    }

                    @Override
                    public void onUpdate(V1Pod oldPod, V1Pod newPod) {
                        log.info("Pod: {} Update", newPod.getMetadata().getName());
                        eventHandler.handleEvt(newPod);
                    }

                    @Override
                    public void onDelete(V1Pod v1Pod, boolean b) {
                        log.info("Pod: {} Deleted", v1Pod.getMetadata().getName());
                    }
                }
        );
        this.factory.startAllRegisteredInformers();
    }

    public void stop() {
        this.factory.stopAllRegisteredInformers();
    }
}
