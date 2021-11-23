package dev.jianmu.workflow.aggregate.definition;

import dev.jianmu.workflow.aggregate.parameter.BoolParameter;
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
 * @description 条件网关
 * @author Ethan Liu
 * @create 2021-01-21 13:14
*/
public class Condition extends BaseNode implements Gateway {
    private Map<Boolean, String> targetMap = new HashMap<>();
    private String expression;
    private ExpressionLanguage expressionLanguage;
    private EvaluationContext context;

    private Condition() {
        this.type = this.getClass().getSimpleName();
    }

    public Map<Boolean, String> getTargetMap() {
        return targetMap;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public void setTargets(Set<String> targets) {
        if (targets.size() > 2) {
            throw new RuntimeException("条件网关下游节点不得超过2个");
        }
        super.setTargets(targets);
    }

    private String getNext() {
        Boolean expResult = false;
        Expression expression = expressionLanguage.parseExpression(this.expression);
        EvaluationResult evaluationResult = expressionLanguage.evaluateExpression(expression, context);
        if (!evaluationResult.isFailure() && evaluationResult.getValue() instanceof BoolParameter) {
            expResult = ((BoolParameter) evaluationResult.getValue()).getValue();
        }
        String targetRef = this.targetMap.get(expResult);
        String target = this.getTargets()
                .stream()
                .filter(nodeRef -> nodeRef.equals(targetRef))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Condition无法找到匹配的节点"));
        return target;
    }

    @Override
    public String calculateTarget(ExpressionLanguage expressionLanguage, EvaluationContext context) {
        this.expressionLanguage = expressionLanguage;
        this.context = context;
        return this.getNext();
    }

    public void setTargetMap(Map<Boolean, String> targetMap) {
        this.targetMap = Map.copyOf(targetMap);
    }

    public void setExpression(String expression) {
        this.expression = expression;
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
        private Map<Boolean, String> targetMap = new HashMap<>();
        private String expression;

        private Builder() {
        }

        public static Builder aCondition() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder targetMap(Map<Boolean, String> targetMap) {
            this.targetMap = targetMap;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder expression(String expression) {
            this.expression = expression;
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

        public Condition build() {
            Condition condition = new Condition();
            condition.name = this.name;
            condition.ref = this.ref;
            condition.expression = this.expression;
            condition.description = this.description;
            condition.sources = this.sources;
            condition.targetMap = this.targetMap;
            return condition;
        }
    }
}
