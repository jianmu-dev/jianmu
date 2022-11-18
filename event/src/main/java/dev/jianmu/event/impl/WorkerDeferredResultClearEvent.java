package dev.jianmu.event.impl;

import lombok.*;

/**
 * @class WorkerDeferredResultClearEvent
 * @description WorkerDeferredResult清除事件
 * @author Daihw
 * @create 2022/9/20 3:23 下午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WorkerDeferredResultClearEvent extends BaseEvent {
    private String workerId;
}
