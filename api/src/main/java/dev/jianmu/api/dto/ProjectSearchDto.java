package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @class ProjectSearchDto
 * @description ProjectSearchDto
 * @author Ethan Liu
 * @create 2021-04-27 09:07
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "项目搜索DTO")
public class ProjectSearchDto extends PageDto {
    private String name;
}
