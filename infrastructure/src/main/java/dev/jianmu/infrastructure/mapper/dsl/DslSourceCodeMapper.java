package dev.jianmu.infrastructure.mapper.dsl;

import dev.jianmu.dsl.aggregate.DslSourceCode;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @class: DslSourceCodeMapper
 * @description: DslSourceCodeMapper
 * @author: Ethan Liu
 * @create: 2021-04-25 14:28
 **/
public interface DslSourceCodeMapper {
    @Insert("insert into dsl_source_code(project_id, workflow_ref, workflow_version, dsl_text, created_time, last_modified_by, last_modified_time) " +
            "values(#{projectId}, #{workflowRef}, #{workflowVersion}, #{dslText}, #{createdTime}, #{lastModifiedBy}, #{lastModifiedTime})")
    void add(DslSourceCode dslSourceCode);

    @Select("select * from dsl_source_code where workflow_ref = #{ref} and workflow_version = #{version}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<DslSourceCode> findByRefAndVersion(@Param("ref") String ref, @Param("version") String version);
}
