package dev.jianmu.workflow.aggregate.parameter;

public enum NodeOutputDefinitionEnum {
    START_TIME("start_time", "开始时间", Parameter.Type.STRING),
    EXECUTION_STATUS("execution_status", "执行状态", Parameter.Type.STRING),
    END_TIME("end_time", "结束时间", Parameter.Type.STRING);

    private final String ref;
    private final String name;
    private final Parameter.Type type;

    NodeOutputDefinitionEnum(String ref, String name, Parameter.Type type) {
        this.ref = ref;
        this.name = name;
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public String getName() {
        return name;
    }

    public Parameter.Type getType() {
        return type;
    }
}
