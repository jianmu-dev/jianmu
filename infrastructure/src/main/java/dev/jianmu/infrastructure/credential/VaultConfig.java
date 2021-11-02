package dev.jianmu.infrastructure.credential;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;

import java.net.URI;

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

    @Override
    public VaultEndpoint vaultEndpoint() {
        return VaultEndpoint.from(URI.create(this.credentialProperties.getUrl()));
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        return new TokenAuthentication(this.credentialProperties.getToken());
    }
}
