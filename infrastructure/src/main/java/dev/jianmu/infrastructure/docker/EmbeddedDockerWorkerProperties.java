package dev.jianmu.infrastructure.docker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @class EmbeddedDockerWorkerProperties
 * @description 内置DockerWorker配置类
 * @author Ethan Liu
 * @create 2021-04-14 19:33
*/
@Data
@Component
@ConfigurationProperties(prefix = "embedded.docker-worker")
public class EmbeddedDockerWorkerProperties {
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
}
