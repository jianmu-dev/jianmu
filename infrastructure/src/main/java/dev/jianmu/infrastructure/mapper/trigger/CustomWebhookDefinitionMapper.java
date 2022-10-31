package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    @Select("<script>" +
            "select * from jm_custom_webhook_definition" +
            "<if test='ref != null'>WHERE ref = #{ref}</if>" +
            "</script>")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "owner_name", property = "ownerName")
    @Result(column = "owner_type", property = "ownerType")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "creator_name", property = "creatorName")
    List<CustomWebhookDefinition> findByRef(String ref);

    @Select("select * from jm_custom_webhook_definition where owner_ref = #{ownerRef} and ref = #{ref}")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "owner_name", property = "ownerName")
    @Result(column = "owner_type", property = "ownerType")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "creator_name", property = "creatorName")
    Optional<CustomWebhookDefinition> findByOwnerRefAndRef(@Param("ownerRef") String ownerRef, @Param("ref") String ref);
}
