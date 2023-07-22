package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class StringTemplateTest
 * @description 字符串模版测试
 * @author Ethan Liu
 * @create 2021-09-02 08:35
*/
@DisplayName("字符串模版测试")
public class StringTemplateTest {
    private final ElContext context = new ElContext();

    @Test()
    @DisplayName("数字变量替换测试")
    void tempTest1() {
        this.context.add("a", new BigDecimal("2.3"));
        this.context.add("b", new BigDecimal("5"));
        String eq = "`${a} * ${b}`";
        Object result = El.eval(this.context, eq);
        assertEquals(result, "2.3 * 5");
    }

    @Test()
    @DisplayName("字符串变量替换测试")
    void tempTest2() {
        this.context.add("a", "aaa");
        this.context.add("b", "bbb");
        String eq = "`${a} / ${b}`";
        Object result = El.eval(this.context, eq);
        assertEquals(result, "aaa / bbb");
    }

    @Test()
    @DisplayName("布尔变量替换测试")
    void tempTest3() {
        this.context.add("a", true);
        this.context.add("b", false);
        String eq = "`${a} && ${b}`";
        Object result = El.eval(this.context, eq);
        assertEquals(result, "true && false");
    }

    @Test()
    @DisplayName("多类型变量替换测试")
    void tempTest4() {
        this.context.add("a", "aaa");
        this.context.add("b", new BigDecimal("32.3"));
        this.context.add("c", false);
        String eq = "`${a} != ${b} == ${c}`";
        Object result = El.eval(this.context, eq);
        assertEquals(result, "aaa != 32.3 == false");
    }

    @Test
    @DisplayName("引号转义测试")
    void tempTest5() {
        String eq = "`[\"xxx\", \"xxx\"]`";
        Object result = El.eval(this.context, eq);
        assertEquals(result, "[\"xxx\", \"xxx\"]");
    }

    @Test
    @DisplayName("未找到的变量替换为字符串null测试")
    void tempTest6() {
        this.context.add("a", "aaa");
        this.context.add("b", new BigDecimal("32.3"));
        String eq = "`${a} != ${b} == ${c}`";
        Object result = El.eval(this.context, eq);
        assertEquals(result, "aaa != 32.3 == null");
    }

    @Test
    @DisplayName("多行文本测试")
    void tempTest7() {
        String eq = "`aaa\nbbb\nbbb\nccc`";
        Object result = El.eval(this.context, eq);
        System.out.println(result);
//        assertEquals(result, "aaa != 32.3 == ${c}");
    }
}
