package dev.jianmu.infrastructure.mapper.node;

import dev.jianmu.node.definition.aggregate.NodeDefinitionVersion;
import dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class NodeDefinitionVersionMapper
 * @description NodeDefinitionVersionMapper
 * @author Ethan Liu
 * @create 2021-09-09 12:56
*/
public interface NodeDefinitionVersionMapper {
    @Select("SELECT * FROM jm_hub_node_definition_version WHERE owner_ref = #{ownerRef} and ref = #{ref} and version = #{version}")
    @Result(column = "result_file", property = "resultFile")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "creator_name", property = "creatorName")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "input_parameters", property = "inputParameters", typeHandler = NodeParameterSetTypeHandler.class)
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = NodeParameterSetTypeHandler.class)
    Optional<NodeDefinitionVersion> findByOwnerRefAndRefAndVersion(@Param("ownerRef") String ownerRef, @Param("ref") String ref, @Param("version") String version);

    @Select("SELECT * FROM jm_hub_node_definition_version WHERE owner_ref = #{ownerRef} and ref = #{ref}")
    @Result(column = "result_file", property = "resultFile")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "creator_name", property = "creatorName")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "input_parameters", property = "inputParameters", typeHandler = NodeParameterSetTypeHandler.class)
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = NodeParameterSetTypeHandler.class)
    List<NodeDefinitionVersion> findByOwnerRefAndRef(@Param("ownerRef") String ownerRef, @Param("ref") String ref);

    @Insert("insert into jm_hub_node_definition_version(id, owner_ref, ref, creator_name, creator_ref, version, description, result_file, input_parameters, output_parameters, spec) " +
            "values(#{id}, #{ownerRef}, #{ref}, #{creatorName}, #{creatorRef}, #{version}, #{description}, #{resultFile}, " +
            "#{inputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler}, " +
            "#{outputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler}, " +
            "#{spec})" +
            " ON DUPLICATE KEY UPDATE " +
            "owner_ref=#{ownerRef}, ref=#{ref}, creator_name=#{creatorName}, creator_ref=#{creatorRef}, version=#{version}, result_file=#{resultFile}, " +
            "input_parameters=#{inputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler}, " +
            "output_parameters=#{outputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeParameterSetTypeHandler}, " +
            "spec=#{spec}")
    void saveOrUpdate(NodeDefinitionVersion nodeDefinitionVersion);

    @Delete("delete from jm_hub_node_definition_version where owner_ref = #{ownerRef} and ref = #{ref}")
    void deleteByOwnerRefAndRef(@Param("ownerRef") String ownerRef, @Param("ref") String ref);
}
