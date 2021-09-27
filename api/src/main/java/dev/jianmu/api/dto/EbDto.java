package dev.jianmu.api.dto;

import dev.jianmu.eventbridge.aggregate.Bridge;
import dev.jianmu.eventbridge.aggregate.Source;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @class: EbDto
 * @description: EbDto
 * @author: Ethan Liu
 * @create: 2021-09-26 13:22
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "EbDto")
public class EbDto {
    @NotNull
    private Bridge bridge;
    @NotNull
    private Source source;
    @NotEmpty
    private List<TargetDto> targets;
}
