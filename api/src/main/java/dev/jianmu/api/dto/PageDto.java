package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: PageDto
 * @description: 分页DTO
 * @author: Ethan Liu
 * @create: 2021-04-19 16:42
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分页DTO")
public class PageDto {
    @Schema(required = true)
    private int pageNum;
    @Schema(required = true)
    private int pageSize;
}
