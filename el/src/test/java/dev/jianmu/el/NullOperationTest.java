package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class NullOperationTest
 * @description 空关键字运算测试
 * @author Ethan Liu
 * @create 2021-09-01 12:27
*/
@DisplayName("null关键字测试")
public class NullOperationTest {

    @Test
    @DisplayName("相等运算测试")
    public void eqTest() {
        String ep = "null == null";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("空字符串不等运算测试")
    public void neqTest1() {
        String ep = "\"\" != null";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("数字0不等运算测试")
    public void neqTest2() {
        String ep = "null != 0";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }

    @Test
    @DisplayName("布尔值不等运算测试")
    public void neqTest3() {
        String ep = "null != false";
        Object result = El.eval(ep);
        assertEquals(result, true);
    }
}
