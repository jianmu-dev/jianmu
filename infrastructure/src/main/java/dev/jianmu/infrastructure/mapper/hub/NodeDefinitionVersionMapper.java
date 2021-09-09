package dev.jianmu.infrastructure.mapper.hub;

import dev.jianmu.hub.intergration.aggregate.NodeDefinitionVersion;
import dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @class: NodeDefinitionVersionMapper
 * @description: NodeDefinitionVersionMapper
 * @author: Ethan Liu
 * @create: 2021-09-09 12:56
 **/
public interface NodeDefinitionVersionMapper {
    @Select("SELECT * FROM hub_node_definition_version WHERE id = #{id}")
    @Result(column = "result_file", property = "resultFile")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "creator_name", property = "creatorName")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "input_parameters", property = "inputParameters", typeHandler = NodeParameterSetTypeHandler.class)
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = NodeParameterSetTypeHandler.class)
    Optional<NodeDefinitionVersion> findByRefAndVersion(String ref, String version);

    @Insert("insert into hub_node_definition_version(id, owner_ref, ref, creator_name, creator_ref, version, result_file, type, input_parameters, output_parameters, spec) " +
            "values(#{id}, #{ownerRef}, #{ref}, #{creatorName}, #{creatorRef}, #{version}, #{resultFile}, #{type}, " +
            "#{inputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler}, " +
            "#{outputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler}, " +
            "#{spec})" +
            " ON DUPLICATE KEY UPDATE SET " +
            "owner_ref=#{ownerRef}, ref=#{ref}, creator_name=#{creatorName}, creator_ref=#{creatorRef}, version=#{version}, result_file=#{resultFile}, type=#{type}, " +
            "input_parameters=#{inputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler}, " +
            "output_parameters=#{outputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler}, " +
            "spec=#{spec}" +
            " WHERE id=#{id}")
    void saveOrUpdate(NodeDefinitionVersion nodeDefinitionVersion);
}
