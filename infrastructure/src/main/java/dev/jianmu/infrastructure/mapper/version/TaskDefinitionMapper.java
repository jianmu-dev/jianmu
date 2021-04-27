package dev.jianmu.infrastructure.mapper.version;

import dev.jianmu.version.aggregate.TaskDefinition;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: TaskDefinitionMapper
 * @description: 任务定义Mapper
 * @author: Ethan Liu
 * @create: 2021-04-18 16:05
 **/
public interface TaskDefinitionMapper {
    @Insert("insert into task_definition(id, ref, name, created_time, lastModified_time) " +
            "values(#{id}, #{ref}, #{name}, #{createdTime}, #{lastModifiedTime})")
    void add(TaskDefinition taskDefinition);

    @Select("select * from task_definition where id = #{id}")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "lastModified_time", property = "lastModifiedTime")
    Optional<TaskDefinition> findById(String id);

    @Select("select * from task_definition where ref = #{ref}")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "lastModified_time", property = "lastModifiedTime")
    Optional<TaskDefinition> findByRef(String ref);

    @Delete("delete from task_definition where id = #{id}")
    void delete(TaskDefinition taskDefinition);

    @Update("update task_definition set name = #{name} where id = #{id}")
    void updateName(TaskDefinition taskDefinition);

    @Select("<script>" +
            "SELECT * FROM `task_definition` " +
            "<if test='name != null'> WHERE `name` like concat('%', #{name}, '%')</if>" +
            "</script>")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "lastModified_time", property = "lastModifiedTime")
    List<TaskDefinition> findAll(String name);
}
