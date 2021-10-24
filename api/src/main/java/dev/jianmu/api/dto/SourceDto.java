package dev.jianmu.api.dto;

import dev.jianmu.eventbridge.aggregate.Source;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: SourceDto
 * @description: SourceDto
 * @author: Ethan Liu
 * @create: 2021-10-05 13:00
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "SourceDto")
public class SourceDto {
    private String id;
    private String bridgeId;
    private String name;
    private Source.Type type;
    private String matcher;
}
