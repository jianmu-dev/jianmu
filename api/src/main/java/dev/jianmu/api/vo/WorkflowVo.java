package dev.jianmu.api.vo;

import dev.jianmu.workflow.aggregate.definition.GlobalParameter;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @class WorkflowVo
 * @description WorkflowVo
 * @author Ethan Liu
 * @create 2021-10-31 14:40
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "流程定义VO")
public class WorkflowVo {
    // 显示名称
    private String name;
    // 唯一引用名称
    private String ref;
    // 类型
    private Workflow.Type type;
    // 描述
    private String description;
    // 版本
    private String version;
    // Node列表
    private Set<Node> nodes;
    // 全局参数
    private Set<GlobalParameter> globalParameters;
    // DSL原始内容
    private String dslText;
}
