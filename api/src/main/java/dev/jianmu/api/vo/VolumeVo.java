package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class VolumeVo
 * @description VolumeVo
 * @create 2022/5/19 10:44 上午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "VolumeVo")
public class VolumeVo {
    @Schema(description = "名称")
    private String name;

    @Schema(description = "操作类型")
    private Type type;

    public enum Type {
        CREATION,
        DELETION
    }
}
