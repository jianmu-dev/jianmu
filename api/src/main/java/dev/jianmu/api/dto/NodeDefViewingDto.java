package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Daihw
 * @class NodeDefViewingDto
 * @description NodeDefViewingDto
 * @create 2022/5/6 5:20 下午
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "节点定义列表查询DTO")
public class NodeDefViewingDto extends PageDto {
    @Schema(required = true)
    private Type type;
    @Schema(required = true)
    private String name;

    public String getType() {
        if (type == Type.LOCAL) {
            return "local";
        }
        return null;
    }

    public enum Type {
        // 全部节点
        ALL,
        // 本地节点
        LOCAL
    }
}
