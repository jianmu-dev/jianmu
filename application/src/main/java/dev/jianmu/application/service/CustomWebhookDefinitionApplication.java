package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinition;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.trigger.repository.CustomWebhookDefinitionRepository;
import dev.jianmu.trigger.repository.CustomWebhookDefinitionVersionRepository;
import dev.jianmu.trigger.repository.CustomWebhookInstanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomWebhookDefinitionApplication {
    private final CustomWebhookDefinitionRepository definitionRepository;
    private final CustomWebhookDefinitionVersionRepository definitionVersionRepository;
    private final CustomWebhookInstanceRepository instanceRepository;

    public CustomWebhookDefinitionApplication(CustomWebhookDefinitionRepository definitionRepository,
                                              CustomWebhookDefinitionVersionRepository definitionVersionRepository,
                                              CustomWebhookInstanceRepository instanceRepository
    ) {
        this.definitionRepository = definitionRepository;
        this.definitionVersionRepository = definitionVersionRepository;
        this.instanceRepository = instanceRepository;
    }

    public Optional<CustomWebhookDefinition> findById(String id) {
        return this.definitionRepository.findById(id);
    }

    public List<CustomWebhookDefinition> findAll() {
        return this.definitionRepository.findALl();
    }

    public List<CustomWebhookDefinitionVersion> findVersionByOwnerRefAndRef(String ownerRef, String ref) {
        return this.definitionVersionRepository.findByOwnerRefAndRef(ownerRef, ref);
    }

    public Optional<CustomWebhookDefinitionVersion> findVersionByOwnerRefAndRefAndVersion(String ownerRef, String ref, String version) {
        return this.definitionVersionRepository.findByOwnerRefAndRefAndVersion(ownerRef, ref, version);
    }

    @Transactional
    public void saveOrUpdateWebhookInstance(String triggerId, String type, List<CustomWebhookInstance.EventInstance> eventInstances) {
        var definitionVersion = this.definitionVersionRepository.findByType(type)
                .orElseThrow(() -> new DataNotFoundException("未找到webhook定义版本：" + type));
        this.instanceRepository.saveOrUpdate(CustomWebhookInstance.Builder.aCustomWebhookInstance()
                .triggerId(triggerId)
                .definitionId(definitionVersion.getDefinitionId())
                .versionId(definitionVersion.getId())
                .eventInstances(eventInstances)
                .build());
    }
}
