package dev.jianmu.infrastructure.mapper.workflow;

import dev.jianmu.infrastructure.typehandler.NodeSetTypeHandler;
import dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;


/**
 * @author Ethan Liu
 * @class WorkflowMapper
 * @description 流程DB映射接口
 * @create 2021-03-21 11:24
 */
public interface WorkflowMapper {

    @Select("select * from workflow where ref_version = #{refVersion}")
    @Result(column = "nodes", property = "nodes", typeHandler = NodeSetTypeHandler.class)
    @Result(column = "global_parameters", property = "globalParameters", typeHandler = ParameterSetTypeHandler.class)
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "failure_mode", property = "failureMode")
    @Result(column = "created_time", property = "createdTime")
    Optional<Workflow> findByRefAndVersion(String refVersion);

    @Select("select * from workflow where ref = #{ref}")
    @Result(column = "nodes", property = "nodes", typeHandler = NodeSetTypeHandler.class)
    @Result(column = "global_parameters", property = "globalParameters", typeHandler = ParameterSetTypeHandler.class)
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "failure_mode", property = "failureMode")
    @Result(column = "created_time", property = "createdTime")
    List<Workflow> findByRef(String ref);

    @Insert("insert into workflow(ref_version, ref, version, type, name, description, nodes, global_parameters, dsl_text, failure_mode, created_time)" +
            "values('${ref + version}', #{ref}, #{version}, #{type}, #{name}, #{description}, " +
            "#{nodes, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeSetTypeHandler}, " +
            "#{globalParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler}, " +
            "#{dslText}, #{failureMode}, #{createdTime})")
    void add(Workflow workflow);

    @Delete("delete from workflow where ref_version = #{refVersion}")
    void deleteByRefAndVersion(String refVersion);

    @Delete("delete from workflow where ref = #{ref}")
    void deleteByRef(String ref);
}
