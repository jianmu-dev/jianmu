package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;

import java.util.Optional;

public interface CustomWebhookInstanceRepository {
    void saveOrUpdate(CustomWebhookInstance customWebhookInstance);

    Optional<CustomWebhookInstance> findByTriggerId(String triggerId);
}
