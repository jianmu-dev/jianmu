package dev.jianmu.infrastructure.mybatis.trigger;

import dev.jianmu.infrastructure.mapper.trigger.CustomWebhookInstanceMapper;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.trigger.repository.CustomWebhookInstanceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomWebhookInstanceRepositoryImpl implements CustomWebhookInstanceRepository {
    private final CustomWebhookInstanceMapper instanceMapper;

    public CustomWebhookInstanceRepositoryImpl(CustomWebhookInstanceMapper instanceMapper) {
        this.instanceMapper = instanceMapper;
    }

    @Override
    public void saveOrUpdate(CustomWebhookInstance customWebhookInstance) {
        this.instanceMapper.saveOrUpdate(customWebhookInstance);
    }

    @Override
    public Optional<CustomWebhookInstance> findByTriggerId(String triggerId) {
        return this.instanceMapper.findByTriggerId(triggerId);
    }
}
