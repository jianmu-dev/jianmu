package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class NumberArithmeticTest
 * @description 数字数学运算测试
 * @author Ethan Liu
 * @create 2021-09-01 13:54
*/
@DisplayName("数字数学运算测试")
public class NumberArithmeticTest {

    @Test()
    @DisplayName("整数加法运算测试")
    void plusTest1() {
        String eq = "23 + 2";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("25"));
    }

    @Test()
    @DisplayName("小数加法运算测试")
    void plusTest2() {
        String eq = "22.50 + 2.5";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("25.00"));
    }

    @Test()
    @DisplayName("整数小数加法运算测试")
    void plusTest3() {
        String eq = "22.50 + 2 + 20";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("44.50"));
    }

    @Test()
    @DisplayName("整数减法运算测试")
    void minusTest1() {
        String eq = "22 - 20";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("2"));
    }

    @Test()
    @DisplayName("小数减法运算测试")
    void minusTest2() {
        String eq = "22.12 - 20.45";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("1.67"));
    }

    @Test()
    @DisplayName("整数小数减法运算测试")
    void minusTest3() {
        String eq = "22 - 20.450";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("1.550"));
    }

    @Test()
    @DisplayName("负数减法运算测试")
    void minusTest4() {
        String eq = "2 - 20.45";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("-18.45"));
    }

    @Test()
    @DisplayName("整数乘法运算测试")
    void timesTest1() {
        String eq = "7 * 8";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("56"));
    }

    @Test()
    @DisplayName("小数乘法运算测试")
    void timesTest2() {
        String eq = "7.0 * 8.00";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("56.000"));
    }

    @Test()
    @DisplayName("整数小数乘法运算测试")
    void timesTest3() {
        String eq = "7 * 8.00";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("56.00"));
    }

    @Test()
    @DisplayName("整数除法运算测试")
    void divTest1() {
        String eq = "12 / 4";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("3.00"));
    }

    @Test()
    @DisplayName("小数除法运算测试")
    void divTest2() {
        String eq = "36.36 / 4";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("9.09"));
    }

    @Test()
    @DisplayName("整数取模运算测试")
    void moduloTest1() {
        String eq = "36 % 5";
        Object result = El.eval(eq);
        System.out.println(result);
        assertEquals(result, new BigDecimal("1"));
    }

    @Test()
    @DisplayName("小数取模运算测试")
    void moduloTest2() {
        String eq = "36 % 5.0";
        Object result = El.eval(eq);
        System.out.println(result);
        assertEquals(result, new BigDecimal("1.0"));
    }

    @Test()
    @DisplayName("四则运算测试")
    void fourArithmeticTest() {
        String eq = "(12 + 33) * (3.1 - 3.142) / 0.3";
        Object result = El.eval(eq);
        assertEquals(result, new BigDecimal("-6.30"));
    }
}
