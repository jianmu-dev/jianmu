package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.Connection;

import java.util.List;
import java.util.Set;

/**
 * @class: ConnectionRepository
 * @description: ConnectionRepository
 * @author: Ethan Liu
 * @create: 2021-08-11 15:57
 **/
public interface ConnectionRepository {
    List<Connection> findBySourceId(String sourceId);

    List<Connection> findByTargetId(String targetId);

    void save(Connection connection);

    void saveOrUpdateList(Set<Connection> connections);

    void deleteById(String id);
}
