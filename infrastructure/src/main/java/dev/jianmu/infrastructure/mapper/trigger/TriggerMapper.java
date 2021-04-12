package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.aggregate.Trigger;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: TriggerMapper
 * @description: 触发器Mapper
 * @author: Ethan Liu
 * @create: 2021-04-08 18:30
 **/
public interface TriggerMapper {
    @Insert("insert into `trigger`(id, workflow_id, task_definition_id, workspace, type, category) " +
            "values(#{id}, #{workflowId}, #{taskDefinitionId}, #{workspace}, #{type}, #{category})")
    void add(Trigger trigger);

    @Delete("delete from trigger where id = #{id}")
    void delete(Trigger trigger);

    @Select("select * from trigger where id = #{triggerId}")
    @Result(column = "workflow_id", property = "workflowId")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    Optional<Trigger> findById(@Param("triggerId") String triggerId);

    @Select("select * from trigger")
    @Result(column = "workflow_id", property = "workflowId")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    List<Trigger> findAll();
}
