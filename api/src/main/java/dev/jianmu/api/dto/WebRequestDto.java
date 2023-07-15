package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;

/**
 * @class WebRequestDto
 * @description WebRequestDto
 * @author Ethan Liu
 * @create 2021-11-19 14:55
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "WebRequest DTO")
public class WebRequestDto extends PageDto {
    @NotEmpty(message = "projectId不能为空")
    private String projectId;
}
