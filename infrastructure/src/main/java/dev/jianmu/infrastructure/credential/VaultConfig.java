package dev.jianmu.infrastructure.credential;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.ClientCertificateAuthentication;
import org.springframework.vault.authentication.ClientCertificateAuthenticationOptions;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.ClientHttpRequestFactoryFactory;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.support.ClientOptions;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.vault.support.SslConfiguration.KeyStoreConfiguration;

import java.net.URI;
import java.time.Duration;

/**
 * @class: VaultConfig
 * @description: VaultConfig
 * @author: Ethan Liu
 * @create: 2021-11-02 11:23
 **/
@Configuration
@ConditionalOnProperty(prefix = "credential", name = "type", havingValue = "vault")
public class VaultConfig extends AbstractVaultConfiguration {
    private final CredentialProperties credentialProperties;

    public VaultConfig(CredentialProperties credentialProperties) {
        this.credentialProperties = credentialProperties;
    }

    static SslConfiguration createSslConfiguration(CredentialProperties.VaultProperties.SslProperties ssl) {
        if (ssl == null) {
            return SslConfiguration.unconfigured();
        }
        KeyStoreConfiguration keyStore = KeyStoreConfiguration.unconfigured();
        KeyStoreConfiguration trustStore = KeyStoreConfiguration.unconfigured();
        if (ssl.getKeyStore() != null) {
            if (StringUtils.hasText(ssl.getKeyStorePassword())) {
                keyStore = KeyStoreConfiguration.of(ssl.getKeyStore(), ssl.getKeyStorePassword().toCharArray());
            } else {
                keyStore = KeyStoreConfiguration.of(ssl.getKeyStore());
            }

            if (StringUtils.hasText(ssl.getKeyStoreType())) {
                keyStore = keyStore.withStoreType(ssl.getKeyStoreType());
            }
        }
        if (ssl.getTrustStore() != null) {

            if (StringUtils.hasText(ssl.getTrustStorePassword())) {
                trustStore = KeyStoreConfiguration.of(ssl.getTrustStore(), ssl.getTrustStorePassword().toCharArray());
            } else {
                trustStore = KeyStoreConfiguration.of(ssl.getTrustStore());
            }

            if (StringUtils.hasText(ssl.getTrustStoreType())) {
                trustStore = trustStore.withStoreType(ssl.getTrustStoreType());
            }
        }

        return new SslConfiguration(keyStore, trustStore);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        var clientOptions = new ClientOptions(Duration.ofMillis(this.credentialProperties.getVault().getConnectionTimeout()),
                Duration.ofMillis(this.credentialProperties.getVault().getReadTimeout()));

        var sslConfiguration = VaultConfig.createSslConfiguration(this.credentialProperties.getVault().getSsl());
        return ClientHttpRequestFactoryFactory.create(clientOptions, sslConfiguration);
    }

    @Override
    public ClientFactoryWrapper clientHttpRequestFactoryWrapper() {
        return new ClientFactoryWrapper(this.clientHttpRequestFactory());
    }

    @Override
    public VaultEndpoint vaultEndpoint() {
        return VaultEndpoint.from(URI.create(this.credentialProperties.getVault().getUrl()));
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        if (this.credentialProperties.getVault().getAuthentication() == CredentialProperties.VaultProperties.AuthenticationType.CERT) {
            var options = ClientCertificateAuthenticationOptions.builder()
                    .path(this.credentialProperties.getVault().getSsl().getCertAuthPath())
                    .build();
            return new ClientCertificateAuthentication(options, restOperations());
        } else {
            return new TokenAuthentication(this.credentialProperties.getVault().getToken());
        }
    }
}
