package dev.jianmu.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    private HttpHeaders createHeaders(String path) {
        var headers = new HttpHeaders();
        if (registryProperties.getAk() == null ||
                registryProperties.getSk() == null ||
                registryProperties.getAk().isBlank() ||
                registryProperties.getSk().isBlank()
        ) {
            return headers;
        }
        try {
            var signature = HmacSha1.encrypt(path, registryProperties.getSk());
            headers.add("X-Client-Type", registryProperties.getType());
            headers.add("X-Client-Version", registryProperties.getVersion());
            headers.add("X-Access-Key", registryProperties.getAk());
            headers.add("X-Signature", signature);
            return headers;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("签名失败");
        }
    }

    private <T> T request(String path, Class<T> tClass) {
        var downloadUrl = registryProperties.getUrl() + path;
        var headers = this.createHeaders(path);
        var entity = new HttpEntity<>(headers);
        return this.restTemplate.exchange(downloadUrl, HttpMethod.GET, entity, tClass).getBody();
    }

    public Optional<NodeDefinitionDto> findByRef(String ref) {
        try {
            var path = "/hub/download/node_definitions/" + ref;
            var dto = this.request(path, NodeDefinitionDto.class);
            log.info("已下载节点定义: {} 内容为: {}", ref, dto);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException | NullPointerException e) {
            log.info("未找到节点定义或节点定义内容不正确: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<NodeDefinitionVersionDto> findByRefAndVersion(String ref, String version) {
        try {
            var path = "/hub/download/node_definitions/" + ref + "/versions/" + version;
            var dto = this.request(path, NodeDefinitionVersionDto.class);
            log.info("已下载节点定义版本: {}:{} 内容为: {}", ref, version, dto);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException | NullPointerException e) {
            log.info("未找到节点定义版本或节点定义版本内容不正确: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
