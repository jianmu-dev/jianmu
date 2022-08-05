package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CustomWebhookDefinitionMapper {
    @Select("select * from jm_custom_webhook_definition where id = #{id}")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "owner_name", property = "ownerName")
    @Result(column = "owner_type", property = "ownerType")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "creator_name", property = "creatorName")
    Optional<CustomWebhookDefinition> findById(String id);

    @Select("select * from jm_custom_webhook_definition")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "owner_name", property = "ownerName")
    @Result(column = "owner_type", property = "ownerType")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "creator_name", property = "creatorName")
    List<CustomWebhookDefinition> findAll();
}
