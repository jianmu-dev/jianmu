package dev.jianmu.infrastructure.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import dev.jianmu.embedded.worker.aggregate.EmbeddedWorker;
import dev.jianmu.embedded.worker.aggregate.EmbeddedWorkerTask;
import dev.jianmu.infrastructure.GlobalProperties;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ethan Liu
 * @class Client
 * @description Docker客户端
 * @create 2021-04-13 10:59
 */
@Service
@ConditionalOnProperty(prefix = "jianmu.worker", name = "type", havingValue = "EMBEDDED_DOCKER")
public class EmbeddedDockerWorker implements EmbeddedWorker {
    private String dockerHost;
    private String apiVersion;
    private String registryUsername;
    private String registryPassword;
    private String registryEmail;
    private String registryUrl;
    private String dockerConfig;
    private String dockerCertPath;

    private Boolean dockerTlsVerify;

    private String sockFile;

    private String mirror;

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedDockerWorker.class);
    private DockerClient dockerClient;
    private Map<String, Integer> runStatusMap = new ConcurrentHashMap<>();

    private final ApplicationEventPublisher publisher;

    public EmbeddedDockerWorker(GlobalProperties properties, ApplicationEventPublisher publisher) {
        this.dockerHost = properties.getWorker().getDocker().getDockerHost();
        this.apiVersion = properties.getWorker().getDocker().getApiVersion();
        this.registryUsername = properties.getWorker().getDocker().getRegistryUsername();
        this.registryPassword = properties.getWorker().getDocker().getRegistryPassword();
        this.registryEmail = properties.getWorker().getDocker().getRegistryEmail();
        this.registryUrl = properties.getWorker().getDocker().getRegistryUrl();
        this.dockerConfig = properties.getWorker().getDocker().getDockerConfig();
        this.dockerCertPath = properties.getWorker().getDocker().getDockerCertPath();
        this.dockerTlsVerify = properties.getWorker().getDocker().getDockerTlsVerify();
        this.sockFile = properties.getWorker().getDocker().getSockFile();
        this.mirror = properties.getWorker().getDocker().getMirror();
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
    @Async
    public void runTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter) {
        var spec = embeddedWorkerTask.getSpec();
        // 创建容器参数
        var createContainerCmd = dockerClient.createContainerCmd(spec.getImage(this.mirror))
                .withName(embeddedWorkerTask.getTaskInstanceId());
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
                // 如果要执行docker客户端镜像则挂载宿主机sock文件
                if (spec.getImage().startsWith("docker:") && null != this.sockFile) {
                    mounts.add(
                            new Mount().withType(MountType.BIND)
                                    .withTarget("/var/run/docker.sock")
                                    .withSource(this.sockFile)
                    );
                }
            });
            var hostConfig = HostConfig.newHostConfig().withMounts(mounts);
            createContainerCmd.withHostConfig(hostConfig);
        }
        if (null != spec.getEnv()) {
            String[] envArray;
            // 如果要执行docker客户端镜像则添加DOCKER_HOST环境变量
            if (spec.getImage().startsWith("docker:")) {
                var envs = new ArrayList<>(Arrays.asList(spec.getEnv()));
                envs.add("DOCKER_HOST=" + this.dockerHost);
                envArray = envs.toArray(spec.getEnv());
            } else {
                envArray = spec.getEnv();
            }
            createContainerCmd.withEnv(envArray);
        }
        if (null != spec.getEntrypoint() && spec.getEntrypoint().length > 0) {
            logger.info("Entrypoint is: {}", Arrays.asList(spec.getEntrypoint()));
            createContainerCmd.withEntrypoint(spec.getEntrypoint());
        }
        if (null != spec.getCmd() && spec.getCmd().length > 0) {
            createContainerCmd.withCmd(spec.getCmd());
        }
        // 检查镜像是否存在本地
        boolean imagePull = false;
        try {
            this.dockerClient.inspectImageCmd(spec.getImage(this.mirror)).exec();
        } catch (NotFoundException e) {
            logger.info("镜像不存在，需要下载");
            imagePull = true;
        }
        // 拉取镜像
        if (imagePull) {
            try {
                this.dockerClient.pullImageCmd(spec.getImage(this.mirror)).exec(new ResultCallback.Adapter<>() {
                    @Override
                    public void onNext(PullResponseItem object) {
                        logger.info("镜像下载成功: {} status: {}", object.getId(), object.getStatus());
                    }
                }).awaitCompletion();
            } catch (InterruptedException | RuntimeException e) {
                logger.error("镜像下载失败:", e);
                this.publisher.publishEvent(TaskFailedEvent.builder()
                        .triggerId(embeddedWorkerTask.getTriggerId())
                        .taskId(embeddedWorkerTask.getTaskInstanceId())
                        .errorMsg(e.getMessage())
                        .build());
                Thread.currentThread().interrupt();
                return;
            }
        }
        // 创建容器
        CreateContainerResponse containerResponse;
        try {
            containerResponse = createContainerCmd.exec();
        } catch (RuntimeException e) {
            logger.error("无法创建容器", e);
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskId(embeddedWorkerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            return;
        }
        // 启动容器
        try {
            this.dockerClient.startContainerCmd(containerResponse.getId()).exec();
        } catch (RuntimeException e) {
            logger.error("容器启动失败:", e);
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskId(embeddedWorkerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            return;
        }
        // 发送任务运行中事件
        this.publisher.publishEvent(TaskRunningEvent.builder().taskId(embeddedWorkerTask.getTaskInstanceId()).build());
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
                                logWriter.write(new String(object.getPayload(), StandardCharsets.UTF_8));
                                logWriter.flush();
                            } catch (IOException e) {
                                logger.error("获取容器日志异常:", e);
                            }
                        }
                    }).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("获取容器日志操作被中断:", e);
            try {
                logWriter.close();
            } catch (IOException ioException) {
                logger.error("日志流关闭失败:", e);
            }
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.error("获取容器日志失败", e);
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskId(embeddedWorkerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            try {
                logWriter.close();
            } catch (IOException ioException) {
                logger.error("日志流关闭失败:", e);
            }
            Thread.currentThread().interrupt();
        }
        // 等待容器执行结果
        try {
            this.dockerClient.waitContainerCmd(containerResponse.getId()).exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(WaitResponse object) {
                    logger.info("dockerTask {} status code is: {}", embeddedWorkerTask.getTaskInstanceId(), object.getStatusCode());
                    runStatusMap.put(embeddedWorkerTask.getTaskInstanceId(), object.getStatusCode());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("获取容器执行结果操作被中断:", e);
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.error("获取容器执行结果失败", e);
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskId(embeddedWorkerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            Thread.currentThread().interrupt();
        }
        // 获取容器执行结果文件(JSON,非数组)，转换为任务输出参数
        String resultFile = null;
        if (null != embeddedWorkerTask.getResultFile()) {
            try (
                    var stream = this.dockerClient.copyArchiveFromContainerCmd(containerResponse.getId(), embeddedWorkerTask.getResultFile()).exec();
                    var tarStream = new TarArchiveInputStream(stream);
                    var reader = new BufferedReader(new InputStreamReader(tarStream, StandardCharsets.UTF_8))
            ) {
                var tarArchiveEntry = tarStream.getNextTarEntry();
                if (!tarStream.canReadEntryData(tarArchiveEntry)) {
                    logger.info("不能读取tarArchiveEntry");
                }
                if (!tarArchiveEntry.isFile()) {
                    logger.info("执行结果文件必须是文件类型, 不支持目录或其他类型");
                }
                logger.info("tarArchiveEntry's name: {}", tarArchiveEntry.getName());
                resultFile = IOUtils.toString(reader);
                logger.info("结果文件内容: {}", resultFile);
            } catch (Exception e) {
                logger.warn("无法获取容器执行结果文件: {}", e.getMessage());
                this.publisher.publishEvent(TaskFailedEvent.builder()
                        .triggerId(embeddedWorkerTask.getTriggerId())
                        .taskId(embeddedWorkerTask.getTaskInstanceId())
                        .errorMsg(e.getMessage())
                        .build());
                return;
            }
        }
        // 清除容器
        this.dockerClient.removeContainerCmd(containerResponse.getId())
                .withRemoveVolumes(true)
                .withForce(true)
                .exec();
        // 发送结果通知
        this.publisher.publishEvent(
                TaskFinishedEvent.builder()
                        .triggerId(embeddedWorkerTask.getTriggerId())
                        .taskId(embeddedWorkerTask.getTaskInstanceId())
                        .cmdStatusCode(runStatusMap.get(embeddedWorkerTask.getTaskInstanceId()))
                        .resultFile(resultFile)
                        .build()
        );
    }

    @Override
    public void resumeTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter) {
        // 获取日志
        try {
            this.dockerClient.logContainerCmd(embeddedWorkerTask.getTaskInstanceId())
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTailAll()
                    .withFollowStream(true)
                    .exec(new ResultCallback.Adapter<>() {
                        @Override
                        public void onNext(Frame object) {
                            try {
                                logWriter.write(new String(object.getPayload(), StandardCharsets.UTF_8));
                                logWriter.flush();
                            } catch (IOException e) {
                                logger.error("获取容器日志异常:", e);
                            }
                        }
                    }).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("获取容器日志操作被中断:", e);
            try {
                logWriter.close();
            } catch (IOException ioException) {
                logger.error("日志流关闭失败:", e);
            }
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.error("获取容器日志失败", e);
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskId(embeddedWorkerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            try {
                logWriter.close();
            } catch (IOException ioException) {
                logger.error("日志流关闭失败:", e);
            }
            if (e instanceof NotFoundException) {
                return;
            } else {
                Thread.currentThread().interrupt();
            }
        }
        // 等待容器执行结果
        try {
            this.dockerClient.waitContainerCmd(embeddedWorkerTask.getTaskInstanceId()).exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(WaitResponse object) {
                    logger.info("dockerTask {} status code is: {}", embeddedWorkerTask.getTaskInstanceId(), object.getStatusCode());
                    runStatusMap.put(embeddedWorkerTask.getTaskInstanceId(), object.getStatusCode());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("获取容器执行结果操作被中断:", e);
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.error("获取容器执行结果失败", e);
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(embeddedWorkerTask.getTriggerId())
                    .taskId(embeddedWorkerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            Thread.currentThread().interrupt();
        }
        // 获取容器执行结果文件(JSON,非数组)，转换为任务输出参数
        String resultFile = null;
        if (null != embeddedWorkerTask.getResultFile()) {
            try (
                    var stream = this.dockerClient.copyArchiveFromContainerCmd(embeddedWorkerTask.getTaskInstanceId(), embeddedWorkerTask.getResultFile()).exec();
                    var tarStream = new TarArchiveInputStream(stream);
                    var reader = new BufferedReader(new InputStreamReader(tarStream, StandardCharsets.UTF_8))
            ) {
                var tarArchiveEntry = tarStream.getNextTarEntry();
                if (!tarStream.canReadEntryData(tarArchiveEntry)) {
                    logger.info("不能读取tarArchiveEntry");
                }
                if (!tarArchiveEntry.isFile()) {
                    logger.info("执行结果文件必须是文件类型, 不支持目录或其他类型");
                }
                logger.info("tarArchiveEntry's name: {}", tarArchiveEntry.getName());
                resultFile = IOUtils.toString(reader);
                logger.info("结果文件内容: {}", resultFile);
            } catch (Exception e) {
                logger.error("无法获取容器执行结果文件:", e);
                this.publisher.publishEvent(TaskFailedEvent.builder()
                        .triggerId(embeddedWorkerTask.getTriggerId())
                        .taskId(embeddedWorkerTask.getTaskInstanceId())
                        .errorMsg(e.getMessage())
                        .build());
            }
        }
        // 清除容器
        this.dockerClient.removeContainerCmd(embeddedWorkerTask.getTaskInstanceId())
                .withRemoveVolumes(true)
                .withForce(true)
                .exec();
        // 发送结果通知
        this.publisher.publishEvent(
                TaskFinishedEvent.builder()
                        .triggerId(embeddedWorkerTask.getTriggerId())
                        .taskId(embeddedWorkerTask.getTaskInstanceId())
                        .cmdStatusCode(runStatusMap.get(embeddedWorkerTask.getTaskInstanceId()))
                        .resultFile(resultFile)
                        .build()
        );
    }

    @Override
    public void terminateTask(String triggerId, String taskInstanceId) {
        logger.info("stop task id: {}", taskInstanceId);
        this.dockerClient.stopContainerCmd(taskInstanceId).exec();
    }

    @Override
    public void createVolume(String volumeName, Map<String, dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec> specMap) {
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

    @Override
    public void deleteImage(String imageName) {
        this.dockerClient.removeImageCmd(imageName).exec();
    }

    @Override
    public void updateImage(String imageName) {
        // 检查镜像是否存在本地
        try {
            this.dockerClient.inspectImageCmd(imageName).exec();
        } catch (NotFoundException e) {
            logger.info("镜像不存在，无需更新");
            return;
        }
        // 拉取镜像
        try {
            this.dockerClient.pullImageCmd(imageName).exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(PullResponseItem object) {
                    logger.info("镜像更新成功: {} status: {}", object.getId(), object.getStatus());
                }
            }).awaitCompletion();
        } catch (InterruptedException | RuntimeException e) {
            logger.error("镜像更新失败:", e);
        }
    }
}
