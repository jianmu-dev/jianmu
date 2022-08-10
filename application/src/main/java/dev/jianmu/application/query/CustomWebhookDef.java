package dev.jianmu.application.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinition;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @class WebhookDef
 * @description Webhook定义
 * @author Daihw
 * @create 2022/8/9 2:27 下午
 */
@Getter
@Builder
@Slf4j
public class CustomWebhookDef {
    private final String name;
    private final String description;
    private final String icon;
    private final String ownerRef;
    private final String ownerName;
    private final CustomWebhookDefinition.OwnerType ownerType;
    private final String creatorRef;
    private final String creatorName;
    private final String type;
    private final String webhook;
    @JsonIgnore
    private final List<CustomWebhookDefinitionVersion.Event> events;

    public String toJsonString() {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.warn("Webhook定义Json序列化失败: {}", e.getMessage());
            throw new RuntimeException("Webhook定义Json序列化失败");
        }
    }
}
