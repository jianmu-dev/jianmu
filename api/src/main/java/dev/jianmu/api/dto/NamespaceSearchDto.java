package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @class NamespaceSearchDto
 * @description NamespaceSearchDto
 * @author Ethan Liu
 * @create 2021-04-27 08:45
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Namespace查询DTO")
public class NamespaceSearchDto extends PageDto {
    private String name;
}
