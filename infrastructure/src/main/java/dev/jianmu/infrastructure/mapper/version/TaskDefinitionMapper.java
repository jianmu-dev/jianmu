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
    @Insert("insert into task_definition(id, ref, name) " +
            "values(#{id}, #{ref}, #{name})")
    void add(TaskDefinition taskDefinition);

    @Select("select * from task_definition where id = #{id}")
    Optional<TaskDefinition> findById(String id);

    @Select("select * from task_definition where ref = #{ref}")
    Optional<TaskDefinition> findByRef(String ref);

    @Delete("delete from task_definition where id = #{id}")
    void delete(TaskDefinition taskDefinition);

    @Update("update task_definition set name = #{name} where id = #{id}")
    void updateName(TaskDefinition taskDefinition);

    @Select("select * from task_definition")
    List<TaskDefinition> findAll();
}
