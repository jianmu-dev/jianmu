package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class NumberLogicTest
 * @description 数字逻辑运算测试
 * @author Ethan Liu
 * @create 2021-09-01 15:11
*/
@DisplayName("数字逻辑运算测试")
public class NumberLogicTest {

    @Test()
    @DisplayName("整数相等运算测试")
    void eqTest1() {
        String eq = "12 == 12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("小数相等运算测试")
    void eqTest2() {
        String eq = "12.12 == 12.12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数小数相等运算测试")
    void eqTest3() {
        String eq = "12.00 == 12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数不等运算测试")
    void neqTest1() {
        String eq = "23 != 12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("小数不等运算测试")
    void neqTest2() {
        String eq = "23.00 != 12.12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数大于运算测试")
    void gtTest1() {
        String eq = "23 > 12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("小数大于运算测试")
    void gtTest2() {
        String eq = "23.23 > 12.12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数小数大于运算测试")
    void gtTest3() {
        String eq = "23 > 12.12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数小于运算测试")
    void ltTest1() {
        String eq = "8 < 12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("小数小于运算测试")
    void ltTest2() {
        String eq = "8.23 < 12.5";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数小数小于运算测试")
    void ltTest3() {
        String eq = "8.23 < 12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数大于等于运算测试1")
    void geTest1() {
        String eq = "8 >= 6";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数大于等于运算测试2")
    void geTest2() {
        String eq = "8 >= 8";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("小数大于等于运算测试1")
    void geTest3() {
        String eq = "28.12 >= 6.23";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("小数大于等于运算测试2")
    void geTest4() {
        String eq = "28.12 >= 28.12";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数小于等于运算测试1")
    void leTest1() {
        String eq = "46 <= 98";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("整数小于等于运算测试2")
    void leTest2() {
        String eq = "46 <= 46";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("小数小于等于运算测试1")
    void leTest3() {
        String eq = "46.345 <= 98.67";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }

    @Test()
    @DisplayName("小数小于等于运算测试2")
    void leTest4() {
        String eq = "46.345 <= 46.345";
        Object result = El.eval(eq);
        assertEquals(result, true);
    }
}
