package dev.jianmu.api.vo;

import dev.jianmu.node.definition.aggregate.NodeParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Daihw
 * @class NodeDefVersionVo
 * @description NodeDefVersionVo
 * @create 2022/5/12 4:52 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "节点版本Vo")
public class NodeDefVersionVo {
    @Schema(required = true, description = "归属唯一标识")
    private String ownerRef;
    @Schema(required = true, description = "节点定义唯一表示")
    private String ref;
    @Schema(required = true, description = "创建人名称")
    private String creatorName;
    @Schema(required = true, description = "创建人唯一标识")
    private String creatorRef;
    @Schema(required = true, description = "版本")
    private String version;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "返回文件路径")
    private String resultFile;
    @Schema(description = "输入参数")
    private List<NodeParameter> inputParameters;
    @Schema(description = "输出参数")
    private List<NodeParameter> outputParameters;
    @Schema(required = true, description = "镜像相关信息")
    private String spec;
}
