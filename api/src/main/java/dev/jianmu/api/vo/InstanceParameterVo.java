package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class InstanceParameterVo
 * @description InstanceParameterVo
 * @author Ethan Liu
 * @create 2021-06-09 20:28
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "任务实例参数VO")
public class InstanceParameterVo {
    // 参数唯一引用名称
    private String ref;
    // 输入输出类型
    private String type;
    // 参数类型
    private String valueType;
    // 参数是否必填
    private Boolean required;
    // 参数值
    private String value;
}
