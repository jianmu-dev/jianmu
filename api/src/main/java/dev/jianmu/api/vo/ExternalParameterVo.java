package dev.jianmu.api.vo;

import dev.jianmu.external_parameter.aggregate.ExternalParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author huangxi
 * @class ExternalParameterVo
 * @description ExternalParameterVo
 * @create 2022-07-13 14:25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "外部参数VO")
public class ExternalParameterVo {
    private String id;
    private String ref;
    private String name;
    private ExternalParameter.Type type;
    private String value;
    private String label;
    private LocalDateTime createdTime;
    private LocalDateTime lastModifiedTime;
}
