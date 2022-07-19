package dev.jianmu.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
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
    private Boolean authMode = true;
    private Worker worker = new Worker();
    private TriggerQueue triggerQueue = new TriggerQueue();
    private El el = new El();

    @Data
    @Component
    @Validated
    public class Global {
        @NotNull
        private Record record = new Record();

        @Data
        @Component
        @Validated
        public class Record {
            @NotNull
            private Long max = 9999L;
            @NotNull
            private Boolean autoClean = false;
        }
    }

    @Data
    @Component
    @Validated
    public static class Worker {
        @NotBlank
        private String secret;
        private Registry registry = new Registry();
        private K8s k8s = new K8s();

        @Data
        @Component
        @Validated
        public class Registry{
            private String address;
            private String username;
            private String password;
        }

        @Data
        @Component
        @Validated
        public static class K8s {
            private String namespace = "jianmu";
            private String placeholder = "jianmudev/placeholder:0.3";
            private String keepalive = "alpine:3.13.6";
            // IfNotPresent, Always and Never
            private String imagePullPolicy = "IfNotPresent";
        }
    }

    @NotNull
    public Boolean trace = true;

    @Data
    @Component
    @Validated
    public static class TriggerQueue {
        @NotNull
        public Integer max = 5;
    }

    @Data
    @Component
    @Validated
    public static class El{
        @NotBlank
        public String server;
    }
}
