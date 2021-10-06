package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.Target;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @class: TargetRepository
 * @description: TargetRepository
 * @author: Ethan Liu
 * @create: 2021-08-11 16:06
 **/
public interface TargetRepository {
    Optional<Target> findById(String id);

    Optional<Target> findByRef(String ref);

    Optional<Target> findByDestinationId(String destinationId);

    List<Target> findByBridgeId(String bridgeId);

    void save(Target target);

    void saveOrUpdateList(Set<Target> targets);

    void deleteById(String id);

    void deleteByBridgeId(String bridgeId);
}
