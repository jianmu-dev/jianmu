package dev.jianmu.api.vo;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "WebhookDefinitionVo")
public class WebhookDefinitionVo {
    /**
     * 主键
     */
    private String id;
    /**
     * 唯一标识
     */
    private String ref;
    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 图标
     */
    private String icon;
    /**
     * 拥有者唯一标识
     */
    private String ownerRef;
    /**
     * 拥有者名称
     */
    private String ownerName;
    /**
     * 拥有者类型
     */
    private CustomWebhookDefinition.OwnerType ownerType;
    /**
     * 创建者唯一标识
     */
    private String creatorRef;
    /**
     * 创建者名称
     */
    private String creatorName;
}
