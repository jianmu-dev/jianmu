package dev.jianmu.infrastructure.mybatis.trigger;

import dev.jianmu.infrastructure.mapper.trigger.CustomWebhookDefinitionMapper;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinition;
import dev.jianmu.trigger.repository.CustomWebhookDefinitionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomWebhookDefinitionRepositoryImpl implements CustomWebhookDefinitionRepository {
    private final CustomWebhookDefinitionMapper definitionMapper;

    public CustomWebhookDefinitionRepositoryImpl(CustomWebhookDefinitionMapper definitionMapper) {
        this.definitionMapper = definitionMapper;
    }

    @Override
    public void saveOrUpdate(CustomWebhookDefinition customWebhookDefinition) {

    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public Optional<CustomWebhookDefinition> findById(String id) {
        return this.definitionMapper.findById(id);
    }

    @Override
    public List<CustomWebhookDefinition> findALl() {
        return this.definitionMapper.findAll();
    }

    @Override
    public Optional<CustomWebhookDefinition> findByOwnerRefAndRef(String ownerRef, String ref) {
        return this.definitionMapper.findByOwnerRefAndRef(ownerRef, ref);
    }
}
