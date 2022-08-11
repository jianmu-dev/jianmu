package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.infrastructure.typehandler.WebhookEventInstanceListTypeHandler;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface CustomWebhookInstanceMapper {
    @Insert("insert into jm_custom_webhook_instance(trigger_id, definition_id, version_id, event_instances) " +
            "values(#{triggerId}, #{definitionId}, #{versionId}, " +
            "#{eventInstances, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.WebhookEventInstanceListTypeHandler}) " +
            "ON DUPLICATE KEY UPDATE " +
            "event_instances = #{eventInstances, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.WebhookEventInstanceListTypeHandler}")
    void saveOrUpdate(CustomWebhookInstance customWebhookInstance);

    @Select("select * from jm_custom_webhook_instance where trigger_id = #{triggerId}")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "definition_id", property = "definitionId")
    @Result(column = "version_id", property = "versionId")
    @Result(column = "event_instances", property = "eventInstances", typeHandler = WebhookEventInstanceListTypeHandler.class)
    Optional<CustomWebhookInstance> findByTriggerId(String triggerId);
}
