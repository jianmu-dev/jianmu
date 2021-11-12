package dev.jianmu.infrastructure.quartz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: CronTriggerEvent
 * @description: CronTriggerEvent
 * @author: Ethan Liu
 * @create: 2021-11-10 17:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CronTriggerEvent {
    private String triggerId;
}
