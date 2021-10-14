package dev.jianmu.api.vo;

import dev.jianmu.eventbridge.aggregate.EventParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @class: TargetEventVo
 * @description: TargetEventVo
 * @author: Ethan Liu
 * @create: 2021-10-14 18:10
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "TargetEventVo")
public class TargetEventVo {
    private String id;
    private String bridgeId;
    private String sourceId;
    private String sourceEventId;
    private String connectionEventId;
    private String targetId;
    private String targetRef;
    private String destinationId;
    private PayloadVo payload;
    private Set<EventParameter> eventParameters;
}
