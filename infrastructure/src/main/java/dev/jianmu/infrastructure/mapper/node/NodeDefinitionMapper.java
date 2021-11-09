package dev.jianmu.infrastructure.mapper.node;

import dev.jianmu.node.definition.aggregate.NodeDefinition;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: NodeDefinitionMapper
 * @description: NodeDefinitionMapper
 * @author: Ethan Liu
 * @create: 2021-09-09 12:42
 **/
public interface NodeDefinitionMapper {
    @Select("SELECT * FROM hub_node_definition WHERE id = #{id}")
    @Result(column = "owner_name", property = "ownerName")
    @Result(column = "owner_type", property = "ownerType")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "creator_name", property = "creatorName")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "source_link", property = "sourceLink")
    @Result(column = "document_link", property = "documentLink")
    Optional<NodeDefinition> findById(String id);

    @Select("SELECT * FROM hub_node_definition")
    @Result(column = "owner_name", property = "ownerName")
    @Result(column = "owner_type", property = "ownerType")
    @Result(column = "owner_ref", property = "ownerRef")
    @Result(column = "creator_name", property = "creatorName")
    @Result(column = "creator_ref", property = "creatorRef")
    @Result(column = "source_link", property = "sourceLink")
    @Result(column = "document_link", property = "documentLink")
    List<NodeDefinition> findPage();

    @Insert("insert into hub_node_definition(id, icon, name, owner_name, owner_type, owner_ref, creator_name, creator_ref, type, description, ref, source_link, document_link) " +
            "values(#{id}, #{icon}, #{name}, #{ownerName}, #{ownerType}, #{ownerRef}, #{creatorName}, #{creatorRef}, #{type}, #{description}, #{ref}, #{sourceLink}, #{documentLink})" +
            " ON DUPLICATE KEY UPDATE " +
            "icon=#{icon}, name=#{name}, owner_name=#{ownerName}, owner_type=#{ownerType}, owner_ref=#{ownerRef}, creator_name=#{creatorName}, creator_ref=#{creatorRef}, type=#{type}, description=#{description}, ref=#{ref}, source_link=#{sourceLink}, document_link=#{documentLink}")
    void saveOrUpdate(NodeDefinition nodeDefinition);

    @Delete("delete from hub_node_definition where id = #{id}")
    void deleteById(String id);
}
