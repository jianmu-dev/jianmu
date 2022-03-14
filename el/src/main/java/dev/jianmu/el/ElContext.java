package dev.jianmu.el;

import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.EvaluationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ethan Liu
 * @class Context
 * @description TODO
 * @create 2021-02-25 10:45
 */
public class ElContext implements EvaluationContext {

    private Map<String, Object> map = new HashMap<>();

    public EvaluationContext add(String name, BigDecimal value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, BigDecimal[] value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, String value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, String[] value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, Boolean value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, Boolean[] value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, Integer value) {
        this.map.put(name, BigDecimal.valueOf(value));
        return this;
    }

    public EvaluationContext add(String name, Integer[] value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, Long value) {
        this.map.put(name, BigDecimal.valueOf(value));
        return this;
    }

    public EvaluationContext add(String name, Long[] value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, Float value) {
        this.map.put(name, new BigDecimal(value.toString()));
        return this;
    }

    public EvaluationContext add(String name, Float[] value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, Double value) {
        this.map.put(name, new BigDecimal(value.toString()));
        return this;
    }

    public EvaluationContext add(String name, Double[] value) {
        this.map.put(name, value);
        return this;
    }

    public EvaluationContext add(String name, Parameter value) {
        this.map.put(name, value.getValue());
        return this;
    }

    public EvaluationContext add(String scope, String name, Parameter value) {
        this.map.put(scope + "." + name, value.getValue());
        return this;
    }

    @Override
    public Object getVariable(String variableName) {
        var value = this.map.get(variableName);
        return Objects.requireNonNullElse(value, "null");
    }
}
