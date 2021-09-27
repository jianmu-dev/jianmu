package dev.jianmu.infrastructure.mybatis.eventbridge;

import dev.jianmu.eventbridge.aggregate.Connection;
import dev.jianmu.eventbridge.repository.ConnectionRepository;
import dev.jianmu.infrastructure.mapper.eventbrdige.ConnectionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @class: ConnectionRepositoryImpl
 * @description: ConnectionRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-08-14 19:38
 **/
@Repository
public class ConnectionRepositoryImpl implements ConnectionRepository {
    private final ConnectionMapper connectionMapper;

    public ConnectionRepositoryImpl(ConnectionMapper connectionMapper) {
        this.connectionMapper = connectionMapper;
    }

    @Override
    public List<Connection> findBySourceId(String sourceId) {
        return this.connectionMapper.findBySourceId(sourceId);
    }

    @Override
    public List<Connection> findByTargetId(String targetId) {
        return this.connectionMapper.findByTargetId(targetId);
    }

    @Override
    public void save(Connection connection) {
        this.connectionMapper.save(connection);
    }

    @Override
    public void saveOrUpdateList(Set<Connection> connections) {
        this.connectionMapper.saveOrUpdateList(connections);
    }

    @Override
    public void deleteById(String id) {
        this.connectionMapper.deleteById(id);
    }
}
