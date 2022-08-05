package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.WebhookDefinitionVersionVo;
import dev.jianmu.api.vo.WebhookDefinitionVo;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinition;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @class CustomWebhookDefinitionMapper
 * @description CustomWebhookDefinitionMapper
 * @author Daihw
 * @create 2022/8/3 5:59 下午
 */
@Mapper
public interface CustomWebhookDefinitionMapper {
    CustomWebhookDefinitionMapper INSTANCE = Mappers.getMapper(CustomWebhookDefinitionMapper.class);

    WebhookDefinitionVo toWebhookDefinitionVo(CustomWebhookDefinition customWebhookDefinition);

    WebhookDefinitionVersionVo toWebhookDefinitionVersionVo(CustomWebhookDefinitionVersion customWebhookDefinitionVersion);
}
