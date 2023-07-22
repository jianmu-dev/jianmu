package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class StringTest
 * @description 字符串运算测试
 * @author Ethan Liu
 * @create 2021-09-01 12:38
*/
@DisplayName("字符串运算测试")
public class StringTest {

    @Test
    @DisplayName("相等测试")
    public void eqTest() {
        String ep = "\"abc\" == \"abc\"";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("不等测试")
    public void neqTest1() {
        String ep = "\"abc\" != \"ABC\"";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("不等测试")
    public void neqTest2() {
        String ep = "\"abc\" != \"abc \"";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("相加测试")
    public void plusTest() {
        String ep = "\"abc\" + \"123\"";
        Object result = El.eval(ep);
        assertEquals(result, "abc123");
    }
}
