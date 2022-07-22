package dev.jianmu.api.dto;

import dev.jianmu.external_parameter.aggregate.ExternalParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author huangxi
 * @class ExternalParameterDto
 * @description ExternalParameterDto
 * @create 2022-07-13 14:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "外部参数更新Dto")
public class ExternalParameterUpdatingDto {
    /**
     * id
     */
    @NotBlank(message = "id不能为空")
    private String id;

    /**
     * 名称
     */
    @Size(min = 1, max = 45, message = "参数名称不能超过45个字符")
    private String name;

    /**
     * 参数类型
     */
    @NotNull(message = "请选择参数类型")
    private ExternalParameter.Type type;

    /**
     * 值
     */
    @NotBlank(message = "请输入参数值")
    @Size(max = 512, message = "参数值不能超过512个字符")
    private String value;

    /**
     * 标签
     */
    @NotBlank(message = "请选择或创建参数标签")
    @Size(max = 15, message = "参数标签不能超过15个字符")
    private String label;
}
