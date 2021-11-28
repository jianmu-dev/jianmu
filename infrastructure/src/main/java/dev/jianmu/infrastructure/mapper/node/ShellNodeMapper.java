package dev.jianmu.infrastructure.mapper.node;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @class ShellNodeMapper
 * @description ShellNodeMapper
 * @author Ethan Liu
 * @create 2021-11-09 21:23
 */
public interface ShellNodeMapper {

    @Insert("insert into shell_node_def(id, shell_node) values(#{id}, #{shellNode})")
    void add(@Param("id") String id, @Param("shellNode") String shellNode);

    @Select("select shell_node from shell_node_def where id = #{id}")
    @Result(column = "shell_node", property = "shellNode")
    Optional<String> findById(String id);
}
