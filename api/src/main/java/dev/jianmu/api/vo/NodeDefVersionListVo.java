package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @class NodeDefVersionListVo
 * @description NodeDefVersionListVo
 * @author Daihw
 * @create 2022/5/12 4:39 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "节点定义版本列表Vo")
public class NodeDefVersionListVo {
    @Schema(required = true, description = "版本列表")
    private List<String> versions;
}
