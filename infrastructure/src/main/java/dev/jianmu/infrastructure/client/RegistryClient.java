package dev.jianmu.infrastructure.client;

import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.task.aggregate.MetaData;
import dev.jianmu.task.aggregate.Worker;
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

    public Optional<? extends Definition> findByRefAndVersion(String ref, String version) {
        DockerDefinition definition = null;
        try {
            var dto = this.restTemplate.getForObject(registryProperties.getUrl() + "/definition/" + ref + "/" + version, DefinitionDto.class);
            var type = Worker.Type.valueOf(dto.getType());
            var metaData = MetaData.Builder.aMetaData()
                    .name(dto.getMetaData().getName())
                    .description(dto.getMetaData().getDescription())
                    .icon(dto.getMetaData().getIcon())
                    .group(dto.getMetaData().getGroup())
                    .tags(dto.getMetaData().getTags())
                    .docs(dto.getMetaData().getDocs())
                    .owner(dto.getMetaData().getOwner())
                    .source(dto.getMetaData().getSource())
                    .build();
            if (type.equals(Worker.Type.DOCKER)) {
                definition = DockerDefinition.Builder.aDockerDefinition()
                        .ref(dto.getRef())
                        .version(dto.getVersion())
                        .resultFile(dto.getResultFile())
                        .type(type)
                        .inputParameters(dto.getInputParameters())
                        .outputParameters(dto.getOutputParameters())
                        .metaData(metaData)
                        .spec(dto.getSpec())
                        .build();
            }
        } catch (HttpClientErrorException | NullPointerException e) {
            log.info("未找到组件定义: {}", e.getMessage());
        }
        return Optional.ofNullable(definition);
    }
}
