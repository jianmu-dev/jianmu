package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author huangxi
 * @class ExternalParameterLabelVo
 * @description ExternalParameterLabelVo
 * @create 2022-07-13 16:43
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "外部参数标签VO")
public class ExternalParameterLabelVo {
    private String id;
    private String value;
    private LocalDateTime createdTime;
    private LocalDateTime lastModifiedTime;
}
