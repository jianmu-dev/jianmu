package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.TaskDefinition;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * @class: DefinitionMapper
 * @description: 任务定义Mapper
 * @author: Ethan Liu
 * @create: 2021-03-25 21:40
 **/
public interface TaskDefinitionMapper {
    @Insert("insert into task_definition(key_version,`key`,version,name,description,env_type) " +
            "values('${key + version}', #{key}, #{version}, #{name}, #{description}, #{envType})")
    void add(TaskDefinition taskDefinition);

    @Select("select * from task_definition where key_version = #{keyVersion}")
    @Result(column = "env_type", property = "envType")
    Optional<TaskDefinition> findByKeyVersion(String keyVersion);

    @Select("select * from task_definition where `key` = #{key}")
    @Result(column = "env_type", property = "envType")
    List<TaskDefinition> findByKey(String key);

    @Select("select * from task_definition")
    @Result(column = "env_type", property = "envType")
    List<TaskDefinition> findAll();
}
