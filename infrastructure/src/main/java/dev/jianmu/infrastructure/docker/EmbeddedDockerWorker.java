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
import dev.jianmu.embedded.worker.aggregate.DockerTask;
import dev.jianmu.embedded.worker.aggregate.DockerWorker;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
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
 * @class Client
 * @description Docker客户端
 * @author Ethan Liu
 * @create 2021-04-13 10:59
*/
@Service
@Profile("!test")
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

    private String sockFile;

    private String mirror;

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedDockerWorker.class);
    private DockerClient dockerClient;
    private Map<String, Integer> runStatusMap = new ConcurrentHashMap<>();

    private final ApplicationEventPublisher publisher;

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
        this.sockFile = properties.getSockFile();
        this.mirror = properties.getMirror();
        this.publisher = publisher;
//        this.connect();
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
    public void runTask(DockerTask dockerTask, BufferedWriter logWriter) {
        var spec = dockerTask.getSpec();
        // 创建容器参数
        var createContainerCmd = dockerClient.createContainerCmd(spec.getImage(this.mirror))
                .withName(dockerTask.getTaskInstanceId());
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
                        .triggerId(dockerTask.getTriggerId())
                        .taskId(dockerTask.getTaskInstanceId())
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
                    .triggerId(dockerTask.getTriggerId())
                    .taskId(dockerTask.getTaskInstanceId())
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
                    .triggerId(dockerTask.getTriggerId())
                    .taskId(dockerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            return;
        }
        // 发送任务运行中事件
        this.publisher.publishEvent(TaskRunningEvent.builder().taskId(dockerTask.getTaskInstanceId()).build());
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
                    .triggerId(dockerTask.getTriggerId())
                    .taskId(dockerTask.getTaskInstanceId())
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
                    logger.info("dockerTask {} status code is: {}", dockerTask.getTaskInstanceId(), object.getStatusCode());
                    runStatusMap.put(dockerTask.getTaskInstanceId(), object.getStatusCode());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("获取容器执行结果操作被中断:", e);
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.error("获取容器执行结果失败", e);
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(dockerTask.getTriggerId())
                    .taskId(dockerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            Thread.currentThread().interrupt();
        }
        // 获取容器执行结果文件(JSON,非数组)，转换为任务输出参数
        String resultFile = null;
        if (null != dockerTask.getResultFile()) {
            try (
                    var stream = this.dockerClient.copyArchiveFromContainerCmd(containerResponse.getId(), dockerTask.getResultFile()).exec();
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
                        .triggerId(dockerTask.getTriggerId())
                        .taskId(dockerTask.getTaskInstanceId())
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
                        .triggerId(dockerTask.getTriggerId())
                        .taskId(dockerTask.getTaskInstanceId())
                        .cmdStatusCode(runStatusMap.get(dockerTask.getTaskInstanceId()))
                        .resultFile(resultFile)
                        .build()
        );
    }

    @Override
    public void resumeTask(DockerTask dockerTask, BufferedWriter logWriter) {
        // 获取日志
        try {
            this.dockerClient.logContainerCmd(dockerTask.getTaskInstanceId())
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
                    .triggerId(dockerTask.getTriggerId())
                    .taskId(dockerTask.getTaskInstanceId())
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
            this.dockerClient.waitContainerCmd(dockerTask.getTaskInstanceId()).exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(WaitResponse object) {
                    logger.info("dockerTask {} status code is: {}", dockerTask.getTaskInstanceId(), object.getStatusCode());
                    runStatusMap.put(dockerTask.getTaskInstanceId(), object.getStatusCode());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            logger.error("获取容器执行结果操作被中断:", e);
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.error("获取容器执行结果失败", e);
            this.publisher.publishEvent(TaskFailedEvent.builder()
                    .triggerId(dockerTask.getTriggerId())
                    .taskId(dockerTask.getTaskInstanceId())
                    .errorMsg(e.getMessage())
                    .build());
            Thread.currentThread().interrupt();
        }
        // 获取容器执行结果文件(JSON,非数组)，转换为任务输出参数
        String resultFile = null;
        if (null != dockerTask.getResultFile()) {
            try (
                    var stream = this.dockerClient.copyArchiveFromContainerCmd(dockerTask.getTaskInstanceId(), dockerTask.getResultFile()).exec();
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
                        .triggerId(dockerTask.getTriggerId())
                        .taskId(dockerTask.getTaskInstanceId())
                        .errorMsg(e.getMessage())
                        .build());
            }
        }
        // 清除容器
        this.dockerClient.removeContainerCmd(dockerTask.getTaskInstanceId())
                .withRemoveVolumes(true)
                .withForce(true)
                .exec();
        // 发送结果通知
        this.publisher.publishEvent(
                TaskFinishedEvent.builder()
                        .triggerId(dockerTask.getTriggerId())
                        .taskId(dockerTask.getTaskInstanceId())
                        .cmdStatusCode(runStatusMap.get(dockerTask.getTaskInstanceId()))
                        .resultFile(resultFile)
                        .build()
        );
    }

    @Override
    public void terminateTask(String taskInstanceId) {
        logger.info("stop task id: {}", taskInstanceId);
        this.dockerClient.stopContainerCmd(taskInstanceId).exec();
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
