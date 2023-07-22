package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class BooleanTest
 * @description 布尔值测试
 * @author Ethan Liu
 * @create 2021-09-01 12:48
*/
@DisplayName("布尔值运算测试")
public class BooleanTest {

    @Test
    @DisplayName("相等运算测试1")
    public void eqTest1() {
        String ep = "true == true";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("相等运算测试2")
    public void eqTest2() {
        String ep = "false == false";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("不等运算测试1")
    public void neqTest1() {
        String ep = "true != false";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("不等运算测试2")
    public void neqTest2() {
        String ep = "false != true";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("not运算测试1")
    public void notTest1() {
        String ep = "!true";
        Object result = El.eval(ep);
        assertEquals(result, false);
    }

    @Test
    @DisplayName("not运算测试2")
    public void notTest2() {
        String ep = "!false";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("and运算测试1")
    public void andTest1() {
        String ep = "true && true";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("and运算测试2")
    public void andTest2() {
        String ep = "false && false";
        Object result = El.eval(ep);
        assertEquals(result, false);
    }

    @Test
    @DisplayName("and运算测试3")
    public void andTest3() {
        String ep = "false && true";
        Object result = El.eval(ep);
        assertEquals(result, false);
    }

    @Test
    @DisplayName("or运算测试1")
    public void orTest1() {
        String ep = "true || true";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("or运算测试2")
    public void orTest2() {
        String ep = "true || false";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("or运算测试3")
    public void orTest3() {
        String ep = "false || false";
        Object result = El.eval(ep);
        assertEquals(result, false);
    }

    @Test
    @DisplayName("复合运算测试1")
    public void complexTest1() {
        String ep = "false || false && true";
        Object result = El.eval(ep);
        assertEquals(result, false);
    }

    @Test
    @DisplayName("复合运算测试2")
    public void complexTest2() {
        String ep = "true || false && true";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("复合运算测试3")
    public void complexTest3() {
        String ep = "!true || false && true";
        Object result = El.eval(ep);
        assertEquals(result, false);
    }
}
