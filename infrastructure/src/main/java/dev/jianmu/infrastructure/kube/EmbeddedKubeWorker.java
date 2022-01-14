package dev.jianmu.infrastructure.kube;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ethan Liu
 * @class EmbeddedKubeWorker
 * @description EmbeddedKubeWorker
 * @create 2022-01-09 08:43
 */
@Slf4j
public class EmbeddedKubeWorker {
    private ApiClient client;
    private String defaultNamespace = "jianmu";
    private final String optionScript = "set -e";
    private final String traceScript = "\necho + %s\n%s";

    public EmbeddedKubeWorker() throws IOException, ApiException {
        this.connect();
        this.createNamespace();
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
                .build();
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

    private String createScript(List<String> commands) {
        var sb = new StringBuilder();
        sb.append(optionScript);
        var formatter = new Formatter(sb, Locale.ROOT);
        commands.forEach(cmd -> {
            var escaped = String.format("%s", cmd);
            escaped = escaped.replace("$", "\\$");
            formatter.format(traceScript, escaped, cmd);
        });
        return sb.toString();
    }

    private V1Pod buildPod() {
        V1ObjectMeta meta = new V1ObjectMeta()
                .name("jianmu-pod-1")
                .namespace(defaultNamespace)
                .annotations(null)
                .labels(null);
        var script = this.createScript(List.of("pwd", "ls -a", "sleep 60"));
        V1Container container = new V1Container()
                .name("jianmu-task-id-1")
                .imagePullPolicy("IfNotPresent")
                .image("alpine:3.13.6");
        container.addEnvItem(new V1EnvVar().name("JIANMU_SCRIPT").value(script));
        container.addCommandItem("/bin/sh");
        container.addCommandItem("-c");
        container.addArgsItem("echo \"$JIANMU_SCRIPT\" | /bin/sh");
        V1PodSpec spec = new V1PodSpec()
                .restartPolicy("OnFailure")
                .serviceAccountName("")
                .volumes(null)
                .containers(List.of(container))
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

    private void createPod() throws ApiException {
        GenericKubernetesApi<V1Pod, V1PodList> podClient =
                new GenericKubernetesApi<>(
                        V1Pod.class,
                        V1PodList.class,
                        "",
                        "v1",
                        "pods",
                        this.client);
        var pod = podClient.create(buildPod()).throwsApiException().getObject();
        System.out.println("Pod Created!");
    }

    private void createPod2() throws ApiException {
        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);
        // the CoreV1Api loads default api-client from global configuration.
        CoreV1Api api = new CoreV1Api();
        var pod = api.createNamespacedPod(defaultNamespace, buildPod(), "true", null, null);
        System.out.println("Pod Created!");
    }

    private void deletePod() throws ApiException {
        GenericKubernetesApi<V1Pod, V1PodList> podClient =
                new GenericKubernetesApi<>(
                        V1Pod.class,
                        V1PodList.class,
                        "",
                        "v1",
                        "pods",
                        this.client);
        V1Pod deletedPod = podClient.delete(defaultNamespace, "foo").throwsApiException().getObject();
        if (deletedPod != null) {
            System.out.println(
                    "Received after-deletion status of the requested object, will be deleting in background!");
        }
        System.out.println("Deleted!");
    }

    private void patchPod() throws ApiException {
        GenericKubernetesApi<V1Pod, V1PodList> podClient =
                new GenericKubernetesApi<>(
                        V1Pod.class,
                        V1PodList.class,
                        "",
                        "v1",
                        "pods",
                        this.client);
        V1Pod patchedPod =
                podClient
                        .patch(
                                "default",
                                "foo",
                                V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH,
                                new V1Patch("{\"metadata\":{\"finalizers\":null}}"))
                        .throwsApiException()
                        .getObject();
        System.out.println("Patched!");
    }

    private void listPods() throws ApiException {
        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

        // the CoreV1Api loads default api-client from global configuration.
        CoreV1Api api = new CoreV1Api();

        // invokes the CoreV1Api client
        V1PodList list =
                api.listNamespacedPod(
                        defaultNamespace,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }
    }

    private void watchPod() throws IOException, ApiException {
        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

        // the CoreV1Api loads default api-client from global configuration.
        CoreV1Api api = new CoreV1Api();

        var call = api.readNamespacedPodLogCall(
                "jianmu-pod-1",
                defaultNamespace,
                "jianmu-task-id-1",
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
            System.out.println(res);
        }
        System.out.println("watch done!");
    }

    public static void main(String[] args) {
        try {
            var worker = new EmbeddedKubeWorker();
//            worker.createPod2();
            worker.watchPod();
//            worker.listPods();
//            worker.deletePod();
        } catch (ApiException e) {
            log.warn("ApiException: {}", e.getResponseBody());
        } catch (IOException e) {
            log.error("e:", e);
        }
    }
}
