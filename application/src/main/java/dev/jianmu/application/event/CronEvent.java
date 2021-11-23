package dev.jianmu.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @class CronEvent
 * @description CronEvent
 * @author Ethan Liu
 * @create 2021-11-10 19:24
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CronEvent {
    private String projectId;
    private String schedule;
}
