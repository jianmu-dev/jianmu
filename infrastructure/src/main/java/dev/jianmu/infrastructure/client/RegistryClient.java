package dev.jianmu.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * @class: RestClient
 * @description: RestClient
 * @author: Ethan Liu
 * @create: 2021-06-21 09:05
 **/
@Slf4j
@Service
public class RegistryClient {
    private final RestTemplate restTemplate;
    private final RegistryProperties registryProperties;

    public RegistryClient(RestTemplate restTemplate, RegistryProperties registryProperties) {
        this.restTemplate = restTemplate;
        this.registryProperties = registryProperties;
    }

    public Optional<NodeDefinitionDto> findByRef(String ref) {
        try {
            var downloadUrl = registryProperties.getUrl() + "/download/node_definitions/" + ref;
            log.info("从 {} 下载节点定义", downloadUrl);
            var dto = this.restTemplate.getForObject(downloadUrl, NodeDefinitionDto.class);
            log.info("已下载节点定义: {} 内容为: {}", ref, dto);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException | NullPointerException e) {
            log.info("未找到节点定义或节点定义内容不正确: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<NodeDefinitionVersionDto> findByRefAndVersion(String ref, String version) {
        try {
            var downloadUrl = registryProperties.getUrl() + "/download/node_definitions/" + ref + "/versions/" + version;
            log.info("从 {} 下载节点定义版本", downloadUrl);
            var dto = this.restTemplate.getForObject(downloadUrl, NodeDefinitionVersionDto.class);
            log.info("已下载节点定义版本: {}:{} 内容为: {}", ref, version, dto);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException | NullPointerException e) {
            log.info("未找到节点定义版本或节点定义版本内容不正确: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
