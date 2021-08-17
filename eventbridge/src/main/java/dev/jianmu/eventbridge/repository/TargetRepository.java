package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.Target;

import java.util.Optional;

/**
 * @class: TargetRepository
 * @description: TargetRepository
 * @author: Ethan Liu
 * @create: 2021-08-11 16:06
 **/
public interface TargetRepository {
    Optional<Target> findById(String id);

    void save(Target target);
}
