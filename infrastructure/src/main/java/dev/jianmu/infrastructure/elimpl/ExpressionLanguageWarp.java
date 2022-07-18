package dev.jianmu.infrastructure.elimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.el.El;
import dev.jianmu.el.ElContext;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.workflow.el.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * @author Ethan Liu
 * @class ExpressionLanguageWarp
 * @description 表达式引擎包装类
 * @create 2021-02-21 11:08
 */
@Service
@Slf4j
public class ExpressionLanguageWarp implements ExpressionLanguage {

    private final RestTemplate restTemplate;
    private final GlobalProperties globalProperties;
    private final ObjectMapper objectMapper;


    public ExpressionLanguageWarp(RestTemplate restTemplate, GlobalProperties globalProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.globalProperties = globalProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Expression parseExpression(String expression, ResultType resultType) {
        return new El(expression, resultType);
    }

    @Override
    public EvaluationResult evaluateExpression(Expression expression, EvaluationContext context) {
        try {
            var result = this.eval(expression, (ElContext) context);
            if (result == null) {
                log.warn("表达式执行错误：{}", "返回结果为null");
                return new EvaluationFailure(expression.getExpression(), "返回结果为null");
            }
            return new ElResult(expression.getExpression(), result);
        } catch (HttpClientErrorException e1) {
            String message = e1.getResponseBodyAsString();
            try {
                message = this.objectMapper.readValue(e1.getResponseBodyAsString(), ResultErrorVo.class).getMessage();
            } catch (JsonProcessingException e2) {
                log.warn("json解析错误：{}", e2.getMessage());
            }
            log.warn("表达式执行错误：{}", message);
            return new EvaluationFailure(expression.getExpression(), message);
        } catch (Exception e) {
            log.warn("表达式执行错误：{}", e.getMessage());
            return new EvaluationFailure(expression.getExpression(), e.getMessage());
        }
    }

    private Object eval(Expression expression, ElContext context) {
        var query = new HashMap<String, Object>();
        query.put("params", context.getParams());
        query.put("expression", expression.getExpression());
        query.put("resultType", expression.getResultType());
        var entity = new HttpEntity<>(query);
        return this.restTemplate.exchange(this.globalProperties.getEl().getServer(), HttpMethod.POST, entity, ResultSucceedVo.class).getBody().getValue();
    }
}
