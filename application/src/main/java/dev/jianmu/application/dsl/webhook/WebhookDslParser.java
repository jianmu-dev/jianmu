package dev.jianmu.application.dsl.webhook;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jianmu.application.exception.DslException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Getter
public class WebhookDslParser {

    private Webhook trigger;

    public static WebhookDslParser parse(String dslText) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var parser = new WebhookDslParser();
        try {
            parser = mapper.readValue(dslText, WebhookDslParser.class);
        } catch (IOException e) {
            log.error("DSL解析异常:", e);
            throw new DslException("DSL解析异常");
        }
        return parser;
    }
}
