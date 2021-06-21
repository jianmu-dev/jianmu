package dev.jianmu.infrastructure.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @class: RestClient
 * @description: RestClient
 * @author: Ethan Liu
 * @create: 2021-06-21 09:05
 **/
@Service
public class RegistryClient {
    private final RestTemplate restTemplate;
    private final RegistryProperties registryProperties;

    public RegistryClient(RestTemplate restTemplate, RegistryProperties registryProperties) {
        this.restTemplate = restTemplate;
        this.registryProperties = registryProperties;
    }

    public DefinitionDto findByRefAndVersion(String ref, String version) {
        return this.restTemplate.getForObject(registryProperties.getUrl() + "/definition/" + ref + "/" + version, DefinitionDto.class);
    }
}
