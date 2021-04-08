package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.aggregate.Trigger;

import java.util.List;
import java.util.Optional;

/**
 * @class: TriggerRepository
 * @description: 触发器仓储接口
 * @author: Ethan Liu
 * @create: 2021-04-06 14:57
 **/
public interface TriggerRepository {
    void add(Trigger trigger);

    void delete(Trigger trigger);

    Optional<Trigger> findById(String triggerId);

    List<Trigger> findAll();
}
