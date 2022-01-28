package dev.jianmu.infrastructure.kubernetes;

import io.kubernetes.client.Copy;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Streams;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * @author Ethan Liu
 * @class KubernetesClient
 * @description KubernetesClient
 * @create 2022-01-24 08:59
 */
@Slf4j
public class KubernetesClient {
    private ApiClient client;
    public final String defaultNamespace;

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

    private void createNamespace() throws ApiException {
        var meta = new V1ObjectMeta()
                .name(defaultNamespace)
                .labels(Map.of("name", defaultNamespace));
        var namespace = new V1Namespace()
                .metadata(meta);
        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);
        // the CoreV1Api loads default api-client from global configuration.
        CoreV1Api api = new CoreV1Api();
        var list = api.listNamespace(
                "true",
                null,
                null,
                null,
                "name=" + defaultNamespace,
                null,
                null,
                null,
                null,
                null
        );
        if (list.getItems().isEmpty()) {
            var v1Namespace = api.createNamespace(
                    namespace,
                    "true",
                    null,
                    null);
            System.out.println("Created Namespace");
        }
    }

    public KubernetesClient() throws IOException, ApiException {
        this.defaultNamespace = "jianmu";
        this.connect();
        this.createNamespace();
    }

    public KubernetesClient(String namespace) throws IOException, ApiException {
        this.defaultNamespace = namespace;
        this.connect();
        this.createNamespace();
    }

    public void createConfigMap(V1ConfigMap configMap) throws ApiException {
        CoreV1Api api = new CoreV1Api();
        var map = api.createNamespacedConfigMap(defaultNamespace, configMap, "true", null, null);
        log.info("ConfigMap {} created", map.getMetadata().getName());
    }

    public void replaceConfigMap(String configMapName, V1ConfigMap configMap) throws ApiException {
        CoreV1Api api = new CoreV1Api();
        var map = api.replaceNamespacedConfigMap(configMapName, defaultNamespace, configMap, "true", null, null);
        log.info("ConfigMap {} replaced", map.getMetadata().getName());
    }

    public void deleteConfigMap(String configMapName) throws ApiException {
        CoreV1Api api = new CoreV1Api();
        var status = api.deleteNamespacedConfigMap(configMapName, defaultNamespace, "true", null, null, null, null, null);
        log.info("ConfigMap {} deleted {}", configMapName , status.getStatus());
    }

    public V1ConfigMap getConfigMap(String configMapName) throws ApiException {
        CoreV1Api api = new CoreV1Api();
        return api.readNamespacedConfigMap(configMapName, defaultNamespace, "true");
    }

    public void createPod(V1Pod v1Pod) throws ApiException {
        CoreV1Api api = new CoreV1Api();
        var pod = api.createNamespacedPod(defaultNamespace, v1Pod, "true", null, null);
        log.info("Pod {} create", pod.getMetadata().getName());
    }

    public void deletePod(String podName) throws ApiException {
        GenericKubernetesApi<V1Pod, V1PodList> podClient =
                new GenericKubernetesApi<>(
                        V1Pod.class,
                        V1PodList.class,
                        "",
                        "v1",
                        "pods",
                        this.client);
        V1Pod deletedPod = podClient.delete(defaultNamespace, podName).throwsApiException().getObject();
        if (deletedPod != null) {
            log.info("Received after-deletion status of the requested object, will be deleting in background!");
        }
        log.info("Pod {} deleted!", deletedPod.getMetadata().getName());
    }

    public void updatePod(V1Pod v1Pod) throws ApiException {
        GenericKubernetesApi<V1Pod, V1PodList> podClient =
                new GenericKubernetesApi<>(
                        V1Pod.class,
                        V1PodList.class,
                        "",
                        "v1",
                        "pods",
                        this.client);
        var updatePod = podClient.update(v1Pod).throwsApiException().getObject();
        log.info("Pod {} updated!", updatePod.getMetadata().getName());
    }

    public V1Pod getPod(String podName) throws ApiException {
        GenericKubernetesApi<V1Pod, V1PodList> podClient =
                new GenericKubernetesApi<>(
                        V1Pod.class,
                        V1PodList.class,
                        "",
                        "v1",
                        "pods",
                        this.client);
        return podClient.get(defaultNamespace, podName).throwsApiException().getObject();
    }

    public void fetchLog(String podName, String containerName, BufferedWriter logWriter) throws IOException, ApiException {
        // the CoreV1Api loads default api-client from global configuration.
        CoreV1Api api = new CoreV1Api();

        var call = api.readNamespacedPodLogCall(
                podName,
                defaultNamespace,
                containerName,
                true,
                null,
                null,
                "true",
                null,
                null,
                null,
                null,
                null
        );
        var response = call.execute();
        if (!response.isSuccessful()) {
            throw new ApiException(response.code(), "Logs request failed: " + response.code());
        }
        var inputStream = response.body().byteStream();
        var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String res = null;
        while ((res = reader.readLine()) != null) {
            logWriter.write(res);
            logWriter.write("\n");
            logWriter.flush();
        }
        log.info("Pod {} container {} fetch log done.", podName, containerName);
    }

    public void copyArchivedFromContainer(String podName, String containerName, String filePath) throws IOException, ApiException {
        Copy copy = new Copy();
        InputStream dataStream = copy.copyFileFromPod(defaultNamespace, podName, containerName, filePath);
        Streams.copy(dataStream, System.out);
    }

    public static void main(String[] args) throws IOException, ApiException {
        var client = new KubernetesClient();
        log.info("copy file from container....");
        client.copyArchivedFromContainer("jianmu-pod-1", "jianmu-keepalive", "/result/hosts");
        log.info("copy file from container");
    }
}
