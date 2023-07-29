package dev.jianmu.infrastructure.credential;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * @class VaultProperties
 * @description VaultProperties
 * @author Ethan Liu
 * @create 2021-11-02 09:53
*/
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "credential")
public class CredentialProperties {
    @NotBlank
    private String type;
    private VaultProperties vault;

    @Data
    @Component
    @Validated
    public static class VaultProperties {
        public enum AuthenticationType {
            TOKEN,
            CERT
        }

        @NotNull
        private String url;
        private String vaultEngineName;
        private AuthenticationType authentication;
        private String token;
        private SslProperties ssl;
        private int connectionTimeout = 5000;
        private int readTimeout = 15000;

        @Data
        @Component
        @Validated
        public static class SslProperties {
            private Resource keyStore;
            private String keyStorePassword;
            private Resource trustStore;
            private String trustStorePassword;
            private String keyStoreType;
            private String trustStoreType;
            @NotEmpty
            private String certAuthPath = "cert";
        }
    }
}
