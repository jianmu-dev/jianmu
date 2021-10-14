package dev.jianmu.workflow.aggregate.definition;

import dev.jianmu.workflow.aggregate.parameter.StringParameter;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.EvaluationResult;
import dev.jianmu.workflow.el.Expression;
import dev.jianmu.workflow.el.ExpressionLanguage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @program: workflow
 * @description: Switch网关
 * @author: Ethan Liu
 * @create: 2021-01-21 13:14
 **/
public class SwitchGateway extends BaseNode implements Gateway {
    private String expression;
    // Switch网关条件Case暂时只支持String类型比较，区分大小写
    private Map<String, String> cases = new HashMap<>();
    private ExpressionLanguage expressionLanguage;
    private EvaluationContext context;

    private SwitchGateway() {
        this.type = this.getClass().getSimpleName();
    }

    public String getExpression() {
        return expression;
    }

    public Map<String, String> getCases() {
        return cases;
    }

    private String getNext() {
        // TODO expression 表达式求值返回String类型的Case，应支持number类型
        String expResult = "";
        Expression expression = this.expressionLanguage.parseExpression(this.expression);
        EvaluationResult evaluationResult = this.expressionLanguage.evaluateExpression(expression, context);
        if (!evaluationResult.isFailure() && evaluationResult.getValue() instanceof StringParameter) {
            expResult = ((StringParameter) evaluationResult.getValue()).getValue();
        }
        // TODO 如果没有匹配case，是否应该使用default
        String targetRef = this.cases.get(expResult);
        String target = this.getTargets()
                .stream()
                .filter(nodeRef -> nodeRef.equals(targetRef))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Switch无法找到匹配的节点"));
        return target;
    }

    @Override
    public String calculateTarget(ExpressionLanguage expressionLanguage, EvaluationContext context) {
        this.expressionLanguage = expressionLanguage;
        this.context = context;
        return this.getNext();
    }

    public static final class Builder {
        // 显示名称
        protected String name;
        // 唯一引用名称
        protected String ref;
        // 描述
        protected String description;
        // 上游节点列表
        protected Set<String> sources = new HashSet<>();
        private String expression;
        // Switch网关条件Case暂时只支持String类型比较，区分大小写
        private Map<String, String> cases = new HashMap<>();

        private Builder() {
        }

        public static Builder aSwitchGateway() {
            return new Builder();
        }

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder cases(Map<String, String> cases) {
            this.cases = cases;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder sources(Set<String> sources) {
            this.sources = sources;
            return this;
        }

        public SwitchGateway build() {
            SwitchGateway switchGateway = new SwitchGateway();
            switchGateway.expression = this.expression;
            switchGateway.name = this.name;
            switchGateway.ref = this.ref;
            switchGateway.cases = this.cases;
            switchGateway.description = this.description;
            switchGateway.sources = this.sources;
            return switchGateway;
        }
    }
}
