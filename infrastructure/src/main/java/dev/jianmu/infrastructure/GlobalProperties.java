package dev.jianmu.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * @author Ethan Liu
 * @class GlobalProperties
 * @description 全局配置
 * @create 2022-01-04 08:48
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "jianmu")
public class GlobalProperties {
    @NotNull
    private Global global = new Global();
    private JwtProperties api;
    private Worker worker;

    @Data
    @Component
    @Validated
    public static class Global {
        @NotNull
        private Record record = new Record();

        @Data
        @Component
        @Validated
        public static class Record {
            @NotNull
            private Long max = 9999L;
            @NotNull
            private Boolean autoClean = false;
        }
    }

    @Data
    @Component
    @Validated
    public static class JwtProperties {
        private String jwtSecret = "Hah123hh46lfmhhs342dasdar56hgfghjn34jlhj54";
        private int jwtExpirationMs = 86400000;
        private String adminUser = "admin";
        @NotNull
        private String adminPasswd;
    }

    @Data
    @Component
    @Validated
    public static class Worker {
        public enum Type {
            DUMB,
            EMBEDDED_DOCKER,
            EMBEDDED_KUBE
        }

        @NotNull
        private Type type = Type.EMBEDDED_DOCKER;
        private Docker docker;
        private K8s k8s = new K8s();

        @Data
        @Component
        @Validated
        public static class Docker {
            @NotNull
            private String dockerHost = "tcp://127.0.0.1:2375";
            private String apiVersion = "v1.39";
            private String registryUsername;
            private String registryPassword;
            private String registryEmail;
            private String registryUrl;
            private String dockerConfig;
            private String dockerCertPath;
            private Boolean dockerTlsVerify;
            private String sockFile;
        }

        @Data
        @Component
        @Validated
        public static class K8s {
            private Resource kubeConfigPath;
            private String namespace = "jianmu";
        }
    }
}
