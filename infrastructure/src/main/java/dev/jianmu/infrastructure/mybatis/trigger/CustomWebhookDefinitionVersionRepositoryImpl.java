package dev.jianmu.infrastructure.mybatis.trigger;

import dev.jianmu.infrastructure.mapper.trigger.CustomWebhookDefinitionVersionMapper;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import dev.jianmu.trigger.repository.CustomWebhookDefinitionVersionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomWebhookDefinitionVersionRepositoryImpl implements CustomWebhookDefinitionVersionRepository {
    private final CustomWebhookDefinitionVersionMapper definitionVersionMapper;

    public CustomWebhookDefinitionVersionRepositoryImpl(CustomWebhookDefinitionVersionMapper definitionVersionMapper) {
        this.definitionVersionMapper = definitionVersionMapper;
    }

    @Override
    public void saveOrUpdate(CustomWebhookDefinitionVersion nodeDefinitionVersion) {
        this.definitionVersionMapper.saveOrUpdate(nodeDefinitionVersion);
    }

    @Override
    public void deleteByOwnerRefAndRef(String ownerRef, String ref) {

    }

    @Override
    public Optional<CustomWebhookDefinitionVersion> findByType(String type) {
        var ownerRef = this.getOwnerRef(type);
        var ref = this.getRef(type);
        var version = this.getVersion(type);
        return this.definitionVersionMapper.findByOwnerRefAndRefAndVersion(ownerRef, ref, version);
    }

    @Override
    public Optional<CustomWebhookDefinitionVersion> findByOwnerRefAndRefAndVersion(String ownerRef, String ref, String version) {
        return this.definitionVersionMapper.findByOwnerRefAndRefAndVersion(ownerRef, ref, version);
    }

    @Override
    public List<CustomWebhookDefinitionVersion> findByOwnerRefAndRef(String ownerRef, String ref) {
        return this.definitionVersionMapper.findByOwnerRefAndRef(ownerRef, ref);
    }

    private String getOwnerRef(String type) {
        var ref = type.split("@")[0];
        var strings = ref.split("/");
        if (strings.length == 1) {
            return "_";
        }
        return strings[0];
    }

    private String getRef(String type) {
        var ref = type.split("@")[0];
        var strings = ref.split("/");
        if (strings.length == 1) {
            return ref;
        }
        return strings[1];
    }

    private String getVersion(String type) {
        return type.split("@")[1];
    }
}
