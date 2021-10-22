package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.event.SourceEvent;

/**
 * @class: SourceEventMapper
 * @description: SourceEventMapper
 * @author: Ethan Liu
 * @create: 2021-10-22 08:42
 **/
public interface SourceEventMapper {
    void add(SourceEvent sourceEvent);
}
