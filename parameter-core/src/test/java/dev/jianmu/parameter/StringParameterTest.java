package dev.jianmu.parameter;

import dev.jianmu.parameter.aggregate.Category;
import dev.jianmu.parameter.aggregate.Scope;
import dev.jianmu.parameter.aggregate.Source;
import dev.jianmu.parameter.aggregate.StringParameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class: StringParameterTest
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-02-11 14:07
 **/
@DisplayName("字符串参数测试类")
public class StringParameterTest {

    @Test
    @DisplayName("创建字符串参数测试")
    void createTest() {
        StringParameter stringParameter = StringParameter.Builder.aStringParameter()
                .name("testStringParameter1")
                .ref("stringParameter1")
                .description("测试字符串参数1")
                .source(Source.Builder.aSource().type(Source.Type.EXTERNAL).value("").build())
                .scope(Scope.Builder.aScope().id("123").category(Category.WORKFLOW_INPUT).build())
                .value("testValue")
                .build();
        assertEquals(stringParameter.getValue(), "testValue");
    }
}
