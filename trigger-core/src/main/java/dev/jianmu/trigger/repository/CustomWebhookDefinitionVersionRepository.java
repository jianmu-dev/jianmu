package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;

import java.util.List;
import java.util.Optional;

public interface CustomWebhookDefinitionVersionRepository {
    void saveOrUpdate(CustomWebhookDefinitionVersion nodeDefinitionVersion);

    void deleteByOwnerRefAndRef(String ownerRef, String ref);

    Optional<CustomWebhookDefinitionVersion> findByType(String type);

    Optional<CustomWebhookDefinitionVersion> findByOwnerRefAndRefAndVersion(String ownerRef, String ref, String version);

    List<CustomWebhookDefinitionVersion> findByOwnerRefAndRef(String ownerRef, String ref);
}
