package dev.jianmu.api.dto;

import dev.jianmu.external_parameter.aggregate.ExternalParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
@Schema(description = "外部参数创建Dto")
public class ExternalParameterCreatingDto {
    /**
     * 唯一标识
     */
    @NotBlank(message = "请输入唯一标识")
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]{0,29}", message = "唯一标识以英文字母或下划线开头，支持下划线、数字、英文字母")
    @Size(min = 1, max = 30, message = "唯一标识不能超过30个字符")
    private String ref;

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
    @Size(max = 30, message = "参数标签不能超过30个字符")
    private String label;
}
