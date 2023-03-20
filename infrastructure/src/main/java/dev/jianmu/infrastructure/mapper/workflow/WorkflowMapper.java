package dev.jianmu.infrastructure.mapper.workflow;

import dev.jianmu.infrastructure.typehandler.NodeSetTypeHandler;
import dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler;
import dev.jianmu.infrastructure.typehandler.StringListTypeHandler;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;


/**
 * @author Ethan Liu
 * @class WorkflowMapper
 * @description 流程DB映射接口
 * @create 2021-03-21 11:24
 */
public interface WorkflowMapper {

    @Select("select * from jm_workflow where ref_version = #{refVersion}")
    @Result(column = "caches", property = "caches", typeHandler = StringListTypeHandler.class)
    @Result(column = "nodes", property = "nodes", typeHandler = NodeSetTypeHandler.class)
    @Result(column = "global_parameters", property = "globalParameters", typeHandler = ParameterSetTypeHandler.class)
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "created_time", property = "createdTime")
    Optional<Workflow> findByRefAndVersion(String refVersion);

    @Select("<script>" +
            "select * from jm_workflow WHERE `ref_version` IN " +
            "<foreach collection='refVersions' item='i'  open='(' separator=',' close=')'>#{i}" +
            "</foreach>" +
            "</script>")
    @Result(column = "caches", property = "caches", typeHandler = StringListTypeHandler.class)
    @Result(column = "nodes", property = "nodes", typeHandler = NodeSetTypeHandler.class)
    @Result(column = "global_parameters", property = "globalParameters", typeHandler = ParameterSetTypeHandler.class)
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "created_time", property = "createdTime")
    List<Workflow> findByRefAndVersions(@Param("refVersions") List<String> refVersions);

    @Select("select * from jm_workflow where ref = #{ref}")
    @Result(column = "caches", property = "caches", typeHandler = StringListTypeHandler.class)
    @Result(column = "nodes", property = "nodes", typeHandler = NodeSetTypeHandler.class)
    @Result(column = "global_parameters", property = "globalParameters", typeHandler = ParameterSetTypeHandler.class)
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "created_time", property = "createdTime")
    List<Workflow> findByRef(String ref);

    @Insert("insert into jm_workflow(ref_version, ref, version, type, tag, name, description, caches, nodes, global_parameters, dsl_text, created_time)" +
            "values('${ref + version}', #{ref}, #{version}, #{type}, #{tag}, #{name}, #{description}, " +
            "#{caches, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.StringListTypeHandler}, " +
            "#{nodes, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeSetTypeHandler}, " +
            "#{globalParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler}, " +
            "#{dslText}, #{createdTime})")
    void add(Workflow workflow);

    @Delete("delete from jm_workflow where ref_version = #{refVersion}")
    void deleteByRefAndVersion(String refVersion);

    @Delete("delete from jm_workflow where ref = #{ref}")
    void deleteByRef(String ref);
}
