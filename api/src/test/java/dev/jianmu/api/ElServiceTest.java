package dev.jianmu.api;

import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.Expression;
import dev.jianmu.workflow.el.ExpressionLanguage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @class ElServiceTest
 * @description 表达式引擎集成测试
 * @author Ethan Liu
 * @create 2021-03-22 15:06
*/
@SpringBootTest(classes = SpringbootApp.class)
@ActiveProfiles("test")
public class ElServiceTest {
    @Resource
    private ExpressionLanguage expressionLanguage;

    @Test
    public void test1() {
        Expression expression = this.expressionLanguage.parseExpression("1+1==2");
        var rs = this.expressionLanguage.evaluateExpression(expression, null);

        assertEquals(rs.getValue().getType().name(), Parameter.Type.BOOL.name());
        assertEquals(rs.getValue().getValue(), true);
    }
}
