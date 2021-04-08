package dev.jianmu.parameter;

import dev.jianmu.parameter.aggregate.BoolParameter;
import dev.jianmu.parameter.aggregate.Category;
import dev.jianmu.parameter.aggregate.Scope;
import dev.jianmu.parameter.aggregate.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class: BoolParameterTest
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-02-11 14:58
 **/
@DisplayName("布尔参数测试类")
public class BoolParameterTest {

    @Test
    @DisplayName("创建布尔参数测试")
    void createTest() {
        BoolParameter boolParameter = BoolParameter.Builder.aBoolParameter()
                .name("testBoolParameter1")
                .ref("boolParameter1")
                .description("测试布尔参数1")
                .source(Source.Builder.aSource().type(Source.Type.EXP).value("exp").build())
                .scope(Scope.Builder.aScope().id("123").category(Category.WORKFLOW_OUTPUT).build())
                .value(true)
                .build();
        assertEquals(boolParameter.getValue(), true);
    }
}
