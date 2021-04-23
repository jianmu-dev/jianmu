package dev.jianmu.infrastructure.mapper.dsl;

import dev.jianmu.dsl.aggregate.DslReference;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: DslReferenceMapper
 * @description: DSL流程定义关联Mapper
 * @author: Ethan Liu
 * @create: 2021-04-23 11:39
 **/
public interface DslReferenceMapper {
    @Insert("insert into dsl_reference(id, dsl_url, workflow_name, workflow_ref, workflow_version, steps, dsl_text, last_modified_by, last_modified_time) " +
            "values(#{id}, #{dslUrl}, #{workflowName}, #{workflowRef}, #{workflowVersion}, #{steps}, #{dslText}, #{lastModifiedBy}, #{lastModifiedTime})")
    void add(DslReference dslReference);

    @Delete("delete from dsl_reference where workflow_ref = #{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);

    @Update("update dsl_reference set workflow_name = #{workflowName}, workflow_version = #{workflowVersion}, steps = #{steps}, dsl_text = #{dslText} , last_modified_by = #{lastModifiedBy}, last_modified_time = #{lastModifiedTime} " +
            "where workflow_ref = #{workflowRef}")
    void updateByWorkflowRef(DslReference dslReference);

    @Select("select * from dsl_reference where id = #{id}")
    @Result(column = "workflow_name", property = "workflowName")
    @Result(column = "dsl_url", property = "dslUrl")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<DslReference> findById(String id);

    @Select("select * from dsl_reference where workflow_ref = #{workflowRef}")
    @Result(column = "workflow_name", property = "workflowName")
    @Result(column = "dsl_url", property = "dslUrl")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<DslReference> findByWorkflowRef(String workflowRef);

    @Select("select * from dsl_reference")
    @Result(column = "workflow_name", property = "workflowName")
    @Result(column = "dsl_url", property = "dslUrl")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<DslReference> findAll();
}
