package dev.jianmu.el;

import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.EvaluationContext;

import java.util.*;

/**
 * @author Ethan Liu
 * @class Context
 * @description TODO
 * @create 2021-02-25 10:45
 */
public class ElContext implements EvaluationContext {

    private List<ElParam> params = new ArrayList<>();

    public EvaluationContext add(String name, Parameter<?> value) {
        this.params.add(ElParam.aParamVo()
                .key(name)
                .type(value.getType().name())
                .value(value.getValue())
                .build());
        return this;
    }

    public EvaluationContext add(String scope, String name, Parameter<?> value) {
        this.params.add(ElParam.aParamVo()
                .key(scope + "." + name)
                .type(value.getType().name())
                .value(value.getValue())
                .build());
        return this;
    }

    public EvaluationContext add(String name, Object object) {
        this.params.add(ElParam.aParamVo()
                .key(name)
                .type("OBJECT")
                .value(object)
                .build());
        return this;
    }

    public List<ElParam> getParams() {
        return this.params;
    }

    @Override
    public Object getVariable(String variableName) {
        return this.params.stream()
                .filter(elParam -> elParam.getKey().equals(variableName))
                .findFirst()
                .orElse(null);
    }

    public ElContext copy() {
        var context = new ElContext();
        context.params = new ArrayList<>(this.getParams());
        return context;
    }
}
