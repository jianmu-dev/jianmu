package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.infrastructure.typehandler.WebhookEventListTypeHandler;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CustomWebhookDefinitionVersionMapper {
    @Insert("INSERT INTO jm_custom_webhook_definition_version (id, definition_id, ref, owner_ref, version, creator_ref, creator_name, events, dsl_text, created_time, last_modified_time) " +
            "values(#{id}, #{definitionId}, #{ref}, #{ownerRef}, #{version}, #{creatorRef}, #{creatorName}, " +
            "#{events, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.WebhookEventListTypeHandler}, " +
            "#{dslText}, #{createdTime}, #{lastModifiedTime}) " +
            "ON DUPLICATE KEY UPDATE " +
            "events = #{events, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.WebhookEventListTypeHandler}," +
            "dsl_text = #{dslText}, last_modified_time = #{lastModifiedTime}")
    void saveOrUpdate(CustomWebhookDefinitionVersion nodeDefinitionVersion);

    @Select("select * from jm_custom_webhook_definition_version where owner_ref = #{ownerRef} and ref = #{ref} order by created_time desc")
    @Result(column = "definition_id", property = "definitionId")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "creator_name", property = "creatorName")
    @Result(column = "events", property = "events", typeHandler = WebhookEventListTypeHandler.class)
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<CustomWebhookDefinitionVersion> findByOwnerRefAndRef(@Param("ownerRef") String ownerRef, @Param("ref") String ref);

    @Select("select * from jm_custom_webhook_definition_version where owner_ref = #{ownerRef} and ref = #{ref} and version = #{version}")
    @Result(column = "definition_id", property = "definitionId")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "creator_name", property = "creatorName")
    @Result(column = "events", property = "events", typeHandler = WebhookEventListTypeHandler.class)
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<CustomWebhookDefinitionVersion> findByOwnerRefAndRefAndVersion(@Param("ownerRef") String ownerRef, @Param("ref") String ref, @Param("version") String version);
}
