package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class TypeCastingTest
 * @description 隐式类型转换测试
 * @author Ethan Liu
 * @create 2021-09-01 22:12
*/
@DisplayName("隐式类型转换测试")
public class TypeCastingTest {

    @Test
    @DisplayName("数字转字符串拼接1")
    void numberCastingTest1() {
        String ep = "123 + \"abc\"";
        Object result = El.eval(ep);
        assertEquals(result, "123abc");
    }

    @Test
    @DisplayName("数字转字符串拼接2")
    void numberCastingTest2() {
        String ep = "\"abc\" + 123";
        Object result = El.eval(ep);
        assertEquals(result, "abc123");
    }

    @Test
    @DisplayName("数字转字符串拼接3")
    void numberCastingTest3() {
        String ep = "\"abc\" + 123.24";
        Object result = El.eval(ep);
        assertEquals(result, "abc123.24");
    }

    @Test
    @DisplayName("数字转字符串拼接4")
    void numberCastingTest4() {
        String ep = "123.24 + \"abc\"";
        Object result = El.eval(ep);
        assertEquals(result, "123.24abc");
    }

    @Test
    @DisplayName("布尔转字符串拼接1")
    void boolCastingTest1() {
        String ep = "true + \"abc\"";
        Object result = El.eval(ep);
        assertEquals(result, "trueabc");
    }

    @Test
    @DisplayName("布尔转字符串拼接2")
    void boolCastingTest2() {
        String ep = "\"abc\" + false";
        Object result = El.eval(ep);
        assertEquals(result, "abcfalse");
    }

    @Test
    @DisplayName("布尔转字符串拼接3")
    void boolCastingTest3() {
        String ep = "123 + false";
        Object result = El.eval(ep);
        assertEquals(result, "123false");
    }

    @Test
    @DisplayName("布尔转字符串拼接4")
    void boolCastingTest4() {
        String ep = "true + 123.12";
        Object result = El.eval(ep);
        assertEquals(result, "true123.12");
    }
}
