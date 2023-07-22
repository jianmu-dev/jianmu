package dev.jianmu.el;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @class PreCompileTest
 * @description 表达式预编译测试
 * @author Ethan Liu
 * @create 2021-09-02 11:25
*/
@DisplayName("表达式预编译测试")
public class PreCompileTest {
    private final ElContext context = new ElContext();

    @Test
    void test() {
        El exp = new El("${a}*10");
        this.context.add("a", 2);
        Object res = exp.eval(this.context);
        System.out.println(res);
    }
}
