package dev.jianmu.application.service;

import dev.jianmu.application.dsl.CustomWebhookDslParser;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.CustomWebhookDef;
import dev.jianmu.project.aggregate.Project;
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
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<CustomWebhookDefinition> findByRef(String ref) {
        return this.definitionRepository.findByRef(ref);
    }

    public List<CustomWebhookDefinitionVersion> findVersionByOwnerRefAndRef(String ownerRef, String ref) {
        return this.definitionVersionRepository.findByOwnerRefAndRef(ownerRef, ref);
    }

    public Optional<CustomWebhookDefinitionVersion> findVersionByOwnerRefAndRefAndVersion(String ownerRef, String ref, String version) {
        return this.definitionVersionRepository.findByOwnerRefAndRefAndVersion(ownerRef, ref, version);
    }

    public CustomWebhookDef findByType(String type) {
        var ownerRef = this.getOwnerRef(type);
        var ref = this.getRef(type);
        var version = this.getVersion(type);
        var definition = this.definitionRepository.findByOwnerRefAndRef(ownerRef, ref)
                .orElseThrow(() -> new DataNotFoundException("未找到自定义Webhook: " + type));
        var definitionVersion = this.definitionVersionRepository.findByOwnerRefAndRefAndVersion(ownerRef, ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到自定义Webhook: " + type));
        return CustomWebhookDef.builder()
                .name(definition.getName())
                .description(definition.getDescription())
                .ownerType(definition.getOwnerType())
                .icon(definition.getIcon())
                .creatorName(definition.getCreatorName())
                .creatorRef(definition.getCreatorRef())
                .ownerRef(definition.getOwnerRef())
                .ownerName(definition.getOwnerName())
                .events(definitionVersion.getEvents())
                .type(Project.TriggerType.WEBHOOK.name())
                .webhook(type)
                .build();
    }

    public List<CustomWebhookDef> findByTypes(Set<String> types) {
        return types.stream()
                .map(this::findByType)
                .collect(Collectors.toList());
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

    @Transactional
    public void updateVersion(String dslText) {
        var dslParser = CustomWebhookDslParser.parse(dslText);
        var version = this.definitionVersionRepository.findByType(dslParser.getWebhookType())
                .orElseThrow(() -> new DataNotFoundException("未找到自定义Webhook：" + dslParser.getWebhookType()));
        this.definitionVersionRepository.saveOrUpdate(CustomWebhookDefinitionVersion.Builder.aCustomWebhookDefinitionVersion()
                .id(version.getId())
                .definitionId(version.getDefinitionId())
                .ref(version.getRef())
                .ownerRef(version.getOwnerRef())
                .version(version.getVersion())
                .creatorRef(version.getCreatorRef())
                .creatorName(version.getCreatorName())
                .events(dslParser.getVersionEvents())
                .dslText(dslText)
                .build());
    }
}
