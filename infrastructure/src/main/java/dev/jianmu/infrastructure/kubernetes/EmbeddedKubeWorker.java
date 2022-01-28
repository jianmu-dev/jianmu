package dev.jianmu.infrastructure.kubernetes;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
public class EmbeddedKubeWorker implements PodEventHandler {
    private final String optionScript = "set -e";
    private final String traceScript = "\necho + %s\n%s";
    private final KubernetesClient client = new KubernetesClient();
    private final KubernetesWatcher watcher = new KubernetesWatcher();

    public EmbeddedKubeWorker() throws IOException, ApiException {
        this.watcher.watch(this);
    }

    public void destroy() {
        log.info("watcher stopping......");
        this.watcher.stop();
        log.info("watcher stopped");
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

    private V1Container buildPlaceholder() {
        var resultFile = "/home/result.txt";
        var script = this.createScript(List.of("pwd", "ls -a", "sleep 20", "echo $aaa", "echo $bbb", "echo 1234567890 > " + resultFile));
        script = "ln -s /result/task-id-1.txt " + resultFile + "\n" + script;
        var container = new V1Container()
                .name("jianmu-task-id-1")
                .imagePullPolicy("IfNotPresent")
                .volumeMounts(List.of(new V1VolumeMount().name("jianmu-result").mountPath("/result")))
                .image("drone/placeholder");
        container.addEnvItem(new V1EnvVar().name("JIANMU_SCRIPT").value(script));
        container.addEnvFromItem(new V1EnvFromSource().configMapRef(new V1ConfigMapEnvSource().name("test-configmap")));
        container.addCommandItem("/bin/sh");
        container.addCommandItem("-c");
        container.addArgsItem("echo \"$JIANMU_SCRIPT\" | /bin/sh");
        return container;
    }

    private V1Container buildKeepalive() {
        var container = new V1Container()
                .name("jianmu-keepalive")
                .imagePullPolicy("IfNotPresent")
                .volumeMounts(List.of(new V1VolumeMount().name("jianmu-result").mountPath("/result")))
                .image("alpine:3.13.6");
        container.addCommandItem("/bin/sh");
        container.addCommandItem("-c");
        container.addArgsItem("touch /result/task-id-1.txt && tail -f /dev/null");
        return container;
    }

    private V1PodSpec buildSpec(V1Container container) {
        return new V1PodSpec()
                .restartPolicy("Never")
                .serviceAccountName("")
                .volumes(List.of(new V1Volume().name("jianmu-result").emptyDir(new V1EmptyDirVolumeSource())))
                .containers(List.of(container, buildKeepalive()))
                .initContainers(null)
                .nodeName(null)
                .nodeSelector(null)
                .tolerations(null)
                .imagePullSecrets(null)
                .hostAliases(null)
                .dnsConfig(null);
    }

    private V1Pod buildPod() {
        V1ObjectMeta meta = new V1ObjectMeta()
                .name("jianmu-pod-1")
                .namespace(null)
                .annotations(null)
                .labels(null);
        return new V1Pod()
                .spec(buildSpec(buildPlaceholder()))
                .metadata(meta);
    }

    private V1ConfigMap buildConfigMap() {
        return new V1ConfigMap().metadata(new V1ObjectMeta().namespace("jianmu").name("test-configmap")).data(Map.of("aaa", "bbb"));
    }

    private V1ConfigMap buildConfigMap2() {
        return new V1ConfigMap().metadata(new V1ObjectMeta().namespace("jianmu").name("test-configmap")).data(Map.of("aaa", "123", "bbb", "456"));
    }

    public static void main(String[] args) {
        try {
            var worker = new EmbeddedKubeWorker();
            worker.client.createConfigMap(worker.buildConfigMap());
            worker.client.createPod(worker.buildPod());
            Thread.sleep(10000);
            log.info("update configMap");
            worker.client.replaceConfigMap("test-configmap", worker.buildConfigMap2());
            log.info("try to get pod-1");
            var pod = worker.client.getPod("jianmu-pod-1");
            log.info("get pod success");
            var containers = pod.getSpec().getContainers();
            for (V1Container c : containers) {
                if (c.getName().equals("jianmu-task-id-1")) {
                    c.setImage("alpine:3.13.6");
                }
            }
            log.info("try to update pod-1 to image: {}", pod.getSpec().getContainers().get(0).getImage());
            worker.client.updatePod(pod);
            log.info("update pod success");
            Thread.sleep(25000);
            log.info("copy file from container....");
            worker.client.copyArchivedFromContainer(pod.getMetadata().getName(), "jianmu-keepalive", "/result/task-id-1.txt");
            log.info("copy file from container");
            Thread.sleep(125000);
            worker.client.deletePod(pod.getMetadata().getName());
            worker.client.deleteConfigMap("test-configmap");
            worker.destroy();
        } catch (ApiException e) {
            e.printStackTrace();
            log.warn("ApiException: {}", e.getResponseBody());
        } catch (IOException | InterruptedException e) {
            log.warn("e: {}", e.getMessage());
        }
    }

    @Override
    public void handleEvt(V1Pod v1Pod) {
        if (!v1Pod.getMetadata().getName().equals("jianmu-pod-1")) {
            log.warn("Unknown Pod");
            return;
        }
        if (v1Pod.getStatus().getContainerStatuses() == null) {
            log.warn("ContainerStatuses is null");
            return;
        }
        this.extractContainers(v1Pod);
    }

    private void extractContainers(V1Pod v1Pod) {
        if (v1Pod == null) {
            return;
        }
        log.info("Pod Phase is: {}", v1Pod.getStatus().getPhase());
        if (v1Pod.getStatus().getContainerStatuses() == null) {
            return;
        }
        v1Pod.getStatus().getContainerStatuses().stream()
                .filter(v1ContainerStatus -> v1ContainerStatus.getName().startsWith("jianmu-task-id"))
                .filter(v1ContainerStatus -> v1ContainerStatus.getState() != null)
                .forEach(v1ContainerStatus -> {
                    if (v1ContainerStatus.getState().getTerminated() != null) {
                        var reason = v1ContainerStatus.getState().getTerminated().getReason();
                        var exitCode = v1ContainerStatus.getState().getTerminated().getExitCode();
                        log.info("Container is terminated, reason: {} exitCode: {}", reason, exitCode);
                    } else if (v1ContainerStatus.getState().getRunning() != null) {
                        try {
                            System.out.println(v1ContainerStatus);
                            var writer = new BufferedWriter(new OutputStreamWriter(System.out));
                            this.client.fetchLog("jianmu-pod-1", v1ContainerStatus.getName(), writer);
                        } catch (IOException | ApiException e) {
                            log.warn("e: {}", e.getMessage());
                        }
                    } else if (v1ContainerStatus.getState().getWaiting() != null) {
                        var reason = v1ContainerStatus.getState().getWaiting().getReason();
                        log.info("Container is waiting, reason: {}", reason);
                    } else {
                        log.info("Container is {}", v1ContainerStatus.getState());
                    }
                });
    }
}
