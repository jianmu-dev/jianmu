package dev.jianmu.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jianmu.dsl.aggregate.DslModel;
import dev.jianmu.dsl.aggregate.Flow;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @class: YamlReader
 * @description: Yaml读取器
 * @author: Ethan Liu
 * @create: 2021-04-16 20:15
 **/
public class YamlReader {
    private static String findVariable(String paramValue) {
        Pattern pattern = Pattern.compile("^\\$\\{([a-zA-Z0-9_-]+)\\}$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String findOutputVariable(String paramValue) {
        Pattern pattern = Pattern.compile("^\\$\\{([a-zA-Z0-9_-]+\\.+[a-zA-Z0-9_-]*)\\}$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String findSecret(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(\\(([a-zA-Z0-9_-]+\\.*[a-zA-Z0-9_-]+)\\)\\)$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        var dsl = mapper.readValue(new File("dsl.yaml"), DslModel.class);
        dsl.syntaxCheck();
        var param = dsl.getParam();
        var flow = new Flow(dsl.getWorkflow());
        flow.getNodes().forEach(node -> {
            node.getParam().forEach((key, val) -> {
                var valName = findVariable(val);
                var secretName = findSecret(val);
                var outputVal = findOutputVariable(val);
                if (null != valName) {
                    var v = param.getOrDefault(valName, "");
                    System.out.println("Param name: " + key + " value is: " + v);
                }
                if (null != secretName) {
                    System.out.println("secretName: " + secretName);
                }
                if (null != outputVal) {
                    System.out.println("output param: " + key + " value is:" + outputVal);
                }
            });
        });
    }

}
