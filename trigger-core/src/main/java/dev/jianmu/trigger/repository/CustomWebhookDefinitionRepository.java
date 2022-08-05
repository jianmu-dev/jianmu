package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinition;

import java.util.List;
import java.util.Optional;

public interface CustomWebhookDefinitionRepository {
    void saveOrUpdate(CustomWebhookDefinition customWebhookDefinition);

    void deleteById(String id);

    Optional<CustomWebhookDefinition> findById(String id);

    List<CustomWebhookDefinition> findALl();
}
