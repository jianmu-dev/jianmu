package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class ComplexTest
 * @description 复合运算测试
 * @author Ethan Liu
 * @create 2021-09-02 09:11
*/
@DisplayName("复合运算测试")
public class ComplexTest {
    private final ElContext context = new ElContext();

    @Test()
    @DisplayName("复合运算测试")
    void complexTest() {
        this.context.add("a", "aaa");
        this.context.add("b", new BigDecimal("32.3"));
        this.context.add("c", false);
        this.context.add("d", new BigDecimal("3"));
        String eq = "${a} +\"---\" +  ${b} * ${d} + \"---\" + `${a} != ${b} == ${c}`";
        Object result = El.eval(this.context, eq);
        assertEquals(result, "aaa---96.9---aaa != 32.3 == false");
    }
}
