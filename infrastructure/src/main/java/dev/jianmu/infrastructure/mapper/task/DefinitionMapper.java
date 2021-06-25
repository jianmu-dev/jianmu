package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.infrastructure.typehandler.ContainerSpecTypeHandler;
import dev.jianmu.infrastructure.typehandler.MetaDataTypeHandler;
import dev.jianmu.infrastructure.typehandler.TaskParameterSetTypeHandler;
import dev.jianmu.task.aggregate.DockerDefinition;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: DefinitionMapper
 * @description: DefinitionMapper
 * @author: Ethan Liu
 * @create: 2021-06-20 22:21
 **/
public interface DefinitionMapper {
    @Insert("insert into task_definition(ref, version, result_file, type, input_parameters, output_parameters, meta_data, spec) " +
            "values(#{ref}, #{version}, #{resultFile}, #{type}, " +
            "#{inputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.TaskParameterSetTypeHandler}, " +
            "#{outputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.TaskParameterSetTypeHandler}, " +
            "#{metaData, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.TaskInstanceListTypeHandler}, " +
            "#{spec, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.TaskInstanceListTypeHandler})")
    void add(DockerDefinition dockerDefinition);

    @Insert("<script>" +
            "insert into task_definition(ref, version, result_file, type, input_parameters, output_parameters, meta_data, spec) values" +
            "<foreach collection='dockerDefinitions' item='i' index='key' separator=','>" +
            "(#{i.ref}, #{i.version}, #{i.resultFile}, #{i.type}, " +
            "#{i.inputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.TaskParameterSetTypeHandler}, " +
            "#{i.outputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.TaskParameterSetTypeHandler}, " +
            "#{i.metaData, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.MetaDataTypeHandler}, " +
            "#{i.spec, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.ContainerSpecTypeHandler})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("dockerDefinitions") List<DockerDefinition> dockerDefinitions);

    @Select("select * from task_definition where ref = #{ref} and version = #{version}")
    @Result(column = "result_file", property = "resultFile")
    @Result(column = "input_parameters", property = "inputParameters", typeHandler = TaskParameterSetTypeHandler.class)
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = TaskParameterSetTypeHandler.class)
    @Result(column = "meta_data", property = "metaData", typeHandler = MetaDataTypeHandler.class)
    @Result(column = "spec", property = "spec", typeHandler = ContainerSpecTypeHandler.class)
    Optional<DockerDefinition> findByRefAndVersion(@Param("ref") String ref, @Param("version") String version);

    @Delete("delete from task_definition where ref = #{ref} and version = #{version}")
    void delete(@Param("ref") String ref, @Param("version") String version);
}
