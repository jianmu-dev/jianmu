package dev.jianmu.infrastructure.kubernetes;

import io.kubernetes.client.Copy;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    private Resource kubeConfigPath;

    private void connect() throws IOException {
        this.client = ClientBuilder.defaultClient();
        Configuration.setDefaultApiClient(client);
    }

    private void connectWithConfig() throws IOException {
        Assert.notNull(this.kubeConfigPath, "kubeConfigPath must not be null");
        Assert.isTrue(this.kubeConfigPath.exists(),
                () -> String.format("Resource %s does not exist", this.kubeConfigPath));
        var c = new FileReader(this.kubeConfigPath.getFile());
        this.client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(c))
                .build();
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

    public KubernetesClient(String namespace) {
        this.defaultNamespace = namespace;
        try {
            this.connect();
            this.createNamespace();
        } catch (IOException e) {
            log.warn("error: {}", e.getMessage());
            throw new RuntimeException("无法连接到Kubernetes集群");
        } catch (ApiException e) {
            log.warn("error: {}", e.getResponseBody());
            throw new RuntimeException("无法连接到Kubernetes集群");
        }
    }

    public KubernetesClient(Resource kubeConfigPath, String namespace) {
        this.defaultNamespace = namespace;
        this.kubeConfigPath = kubeConfigPath;
        try {
            this.connectWithConfig();
            this.createNamespace();
        } catch (IOException e) {
            log.warn("error: {}", e.getMessage());
            throw new RuntimeException("无法连接到Kubernetes集群");
        } catch (ApiException e) {
            log.warn("error: {}", e.getResponseBody());
            throw new RuntimeException("无法连接到Kubernetes集群");
        }
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
        api.deleteCollectionNamespacedConfigMap(
                defaultNamespace,
                "true",
                null,
                null,
                null,
                null,
                "podName=" + configMapName,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        log.info("ConfigMap {} deleted", configMapName);
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

    public String copyArchivedFromContainer(String podName, String containerName, String filePath) throws IOException, ApiException {
        Copy copy = new Copy();
        InputStream dataStream = copy.copyFileFromPod(defaultNamespace, podName, containerName, filePath);
        return IOUtils.toString(dataStream, StandardCharsets.UTF_8);
    }
}
