package dev.jianmu.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import dev.jianmu.dsl.aggregate.DslModel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @class: YamlWriter
 * @description: YamlWriter
 * @author: Ethan Liu
 * @create: 2021-04-16 20:59
 **/
public class YamlWriter {
    public static void main(String[] args) throws Exception {
        var mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var cron = "* 5/* * * * ? *";
        Map<String, Map<String, String>> event = new HashMap<>();
        Map<String, String> p1 = new HashMap<>();
        p1.put("branch", "${branch_name}");
        event.put("push_event", p1);
        Map<String, String> param = new HashMap<>();
        Map<String, Object> workflow = new HashMap<>();
        var dsl = new DslModel();
        dsl.setCron(cron);
        dsl.setEvent(event);
        dsl.setParam(param);
        dsl.setWorkflow(workflow);
        mapper.writeValue(new File("testDsl.yaml"), dsl);
    }
}
