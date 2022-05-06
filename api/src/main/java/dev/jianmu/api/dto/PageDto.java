package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @class PageDto
 * @description 分页DTO
 * @author Ethan Liu
 * @create 2021-04-19 16:42
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分页DTO")
public class PageDto {
    @Schema(required = true)
    private int pageNum;
    @Schema(required = true)
    private int pageSize;
    @Schema(required = true)
    private Type type;

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
