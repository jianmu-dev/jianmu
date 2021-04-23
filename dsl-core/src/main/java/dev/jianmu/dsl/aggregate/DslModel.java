package dev.jianmu.dsl.aggregate;

import java.util.List;
import java.util.Map;

/**
 * @class: DslModel
 * @description: DSL语法模型
 * @author: Ethan Liu
 * @create: 2021-04-16 20:29
 **/
public class DslModel {
    private String cron;
    private Map<String, Map<String, String>> event;
    private Map<String, String> param;
    private Map<String, Object> workflow;

    public void syntaxCheck() {
        if (this.cron.isBlank()) {
            throw new RuntimeException("Cron未设置");
        }
        if (null == this.workflow) {
            throw new RuntimeException("workflow未设置");
        }
        var flow = this.workflow;
        if (null == flow.get("name")) {
            throw new RuntimeException("workflow name未设置");
        }
        if (null == flow.get("ref")) {
            throw new RuntimeException("workflow ref未设置");
        }
        flow.forEach((key, val) -> {
            if (val instanceof Map) {
                this.checkNode(key, (Map<?, ?>) val);
            }
        });
    }

    private void checkNode(String nodeName, Map<?, ?> node) {
        var type = node.get("type");
        if (null == type) {
            throw new RuntimeException("Node type未设置");
        }
        if (type.equals("start")) {
            this.checkStart(node);
            return;
        }
        if (type.equals("end")) {
            this.checkEnd(node);
            return;
        }
        if (type.equals("condition")) {
            this.checkCondition(node);
            return;
        }
        this.checkTask(nodeName, node);
    }

    private void checkTask(String nodeName, Map<?, ?> node) {
        var sources = node.get("sources");
        var targets = node.get("targets");
        var param = node.get("param");
        if (!(sources instanceof List) || ((List<?>) sources).isEmpty()) {
            throw new RuntimeException("任务节点" + nodeName + "sources未设置");
        }
        if (!(targets instanceof List) || ((List<?>) targets).isEmpty()) {
            throw new RuntimeException("任务节点" + nodeName + "targets未设置");
        }
    }

    private void checkCondition(Map<?, ?> node) {
        var expression = node.get("expression");
        var cases = node.get("cases");
        if (null == expression) {
            throw new RuntimeException("条件网关expression未设置");
        }
        if (!(cases instanceof Map<?, ?>)) {
            throw new RuntimeException("条件网关cases未设置");
        }
        if (((Map<?, ?>) cases).size() != 2) {
            throw new RuntimeException("cases数量错误");
        }
    }

    private void checkEnd(Map<?, ?> node) {
        var sources = node.get("sources");
        if (!(sources instanceof List)) {
            throw new RuntimeException("结束节点sources未设置");
        }
    }

    private void checkStart(Map<?, ?> node) {
        var targets = node.get("targets");
        if (!(targets instanceof List)) {
            throw new RuntimeException("开始节点targets未设置");
        }
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Map<String, Map<String, String>> getEvent() {
        return event;
    }

    public void setEvent(Map<String, Map<String, String>> event) {
        this.event = event;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public void setParam(Map<String, String> param) {
        this.param = param;
    }

    public Map<String, Object> getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Map<String, Object> workflow) {
        this.workflow = workflow;
    }
}
