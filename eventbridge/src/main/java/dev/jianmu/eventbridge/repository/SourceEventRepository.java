package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.event.SourceEvent;

/**
 * @class: SourceEventRepository
 * @description: SourceEventRepository
 * @author: Ethan Liu
 * @create: 2021-10-17 09:43
 **/
public interface SourceEventRepository {
    void add(SourceEvent sourceEvent);
}
