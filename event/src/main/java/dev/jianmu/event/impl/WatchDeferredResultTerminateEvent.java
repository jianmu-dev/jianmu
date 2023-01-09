package dev.jianmu.event.impl;

import lombok.*;

/**
 * @class WatchDeferredResultTerminateEvent
 * @description WatchDeferredResult终止事件
 * @author Daihw
 * @create 2023/1/9 3:23 下午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WatchDeferredResultTerminateEvent extends BaseEvent {
    private String workerId;
    private String businessId;
}
