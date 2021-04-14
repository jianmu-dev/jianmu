package dev.jianmu.infrastructure.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import dev.jianmu.task.aggregate.DockerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @class: Client
 * @description: Docker客户端
 * @author: Ethan Liu
 * @create: 2021-04-13 10:59
 **/
@Service
public class EmbeddedDockerWorker implements DockerWorker {
    private String dockerHost;
    private String apiVersion;
    private String registryUsername;
    private String registryPassword;
    private String registryEmail;
    private String registryUrl;
    private String dockerConfig;
    private String dockerCertPath;

    private Boolean dockerTlsVerify;

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedDockerWorker.class);
    private DockerClient dockerClient;

    private final ApplicationEventPublisher publisher;

    @Inject
    public EmbeddedDockerWorker(EmbeddedDockerWorkerProperties properties, ApplicationEventPublisher publisher) {
        this.dockerHost = properties.getDockerHost();
        this.apiVersion = properties.getApiVersion();
        this.registryUsername = properties.getRegistryUsername();
        this.registryPassword = properties.getRegistryPassword();
        this.registryEmail = properties.getRegistryEmail();
        this.registryUrl = properties.getRegistryUrl();
        this.dockerConfig = properties.getDockerConfig();
        this.dockerCertPath = properties.getDockerCertPath();
        this.dockerTlsVerify = properties.getDockerTlsVerify();
        this.publisher = publisher;
        this.connect();
    }

    private void connect() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(this.dockerHost)
                .withApiVersion(this.apiVersion)
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .build();
        DockerClient dockerClient = DockerClientBuilder
                .getInstance(config)
                .withDockerHttpClient(httpClient)
                .build();
        dockerClient.pingCmd().exec();
        this.dockerClient = dockerClient;
    }

    @Override
    public void runTask(DockerTask dockerTask, BufferedWriter logWriter) {
        var spec = dockerTask.getSpec();
        // 创建容器
        var createContainerCmd = dockerClient.createContainerCmd(spec.getImage());
        if (!spec.getWorkingDir().isBlank()) {
            createContainerCmd.withWorkingDir(spec.getWorkingDir());
        }
        if (null != spec.getHostConfig()) {
            List<Mount> mounts = new ArrayList<>();
            spec.getHostConfig().getMounts().forEach(m -> {
                mounts.add(
                        new Mount()
                                .withType(MountType.VOLUME)
                                .withSource(m.getSource())
                                .withTarget(m.getTarget())
                );
            });
            var hostConfig = HostConfig.newHostConfig().withMounts(mounts);
            createContainerCmd.withHostConfig(hostConfig);
        }
        if (null != spec.getEnv()) {
            createContainerCmd.withEnv(spec.getEnv());
        }
        if (null != spec.getEntrypoint()) {
            createContainerCmd.withEntrypoint(spec.getEntrypoint());
        }
        var containerResponse = createContainerCmd.exec();
        // 启动容器
        this.dockerClient.startContainerCmd(containerResponse.getId()).exec();
        // 获取日志
        try {
            this.dockerClient.logContainerCmd(containerResponse.getId())
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTailAll()
                    .withFollowStream(true)
                    .exec(new ResultCallback.Adapter<>() {
                        @Override
                        public void onNext(Frame object) {
                            try {
                                logWriter.write(new String(object.getPayload()));
                                logWriter.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("无法获取容器日志:", e);
            throw new RuntimeException("无法获取容器日志");
        }
        // 等待容器执行结果
        try {
            this.dockerClient.waitContainerCmd(containerResponse.getId()).exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(WaitResponse object) {
                    logger.info("dockerTask {} status code is: {}", dockerTask.getTaskInstanceId(), object.getStatusCode());
                    // TODO 返回任务结果
                    publisher.publishEvent("done");
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("无法获取容器执行结果:", e);
            throw new RuntimeException("无法获取容器执行结果");
        }
        // 清除容器
        this.dockerClient.removeContainerCmd(containerResponse.getId())
                .withRemoveVolumes(true)
                .exec();
    }

    @Override
    public void createVolume(String volumeName) {
        // 创建Volume
        this.dockerClient.createVolumeCmd()
                .withName(volumeName)
                .withDriver("local")
                .exec();
    }

    @Override
    public void deleteVolume(String volumeName) {
        // 清除Volume
        this.dockerClient.removeVolumeCmd(volumeName).exec();
    }
}
