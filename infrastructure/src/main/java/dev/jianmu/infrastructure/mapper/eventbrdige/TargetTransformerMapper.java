package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Transformer;
import dev.jianmu.infrastructure.eventbridge.BodyTransformer;
import dev.jianmu.infrastructure.eventbridge.HeaderTransformer;
import dev.jianmu.infrastructure.eventbridge.QueryTransformer;
import org.apache.ibatis.annotations.*;

import java.util.Set;

/**
 * @class: TargetTransformerMapper
 * @description: TargetTransformerMapper
 * @author: Ethan Liu
 * @create: 2021-08-16 09:26
 **/
public interface TargetTransformerMapper {
    @Select("SELECT * FROM `eb_target_transformer` WHERE target_id = #{targetId}")
    @Result(column = "variable_name", property = "variableName")
    @Result(column = "variable_type", property = "variableType")
    @Result(column = "expression", property = "expression")
    @TypeDiscriminator(column = "class_type", javaType = String.class, cases = {
            @Case(value = "BodyTransformer", type = BodyTransformer.class),
            @Case(value = "HeaderTransformer", type = HeaderTransformer.class),
            @Case(value = "QueryTransformer", type = QueryTransformer.class)
    })
    Set<Transformer> findByTargetId(String targetId);

    @Insert("insert into eb_target_transformer(target_id, variable_name, variable_type, expression, class_type) " +
            "values(#{targetId}, #{tf.variableName}, #{tf.variableType}, #{tf.expression}, #{classType})")
    void save(@Param("targetId") String targetId, @Param("tf") Transformer<?> transformer, @Param("classType") String classType);
}
