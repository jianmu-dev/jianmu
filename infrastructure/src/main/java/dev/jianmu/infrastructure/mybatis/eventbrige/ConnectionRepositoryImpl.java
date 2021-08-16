package dev.jianmu.infrastructure.mybatis.eventbrige;

import dev.jianmu.eventbridge.aggregate.Connection;
import dev.jianmu.eventbridge.repository.ConnectionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @class: ConnectionRepositoryImpl
 * @description: ConnectionRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-08-14 19:38
 **/
@Repository
public class ConnectionRepositoryImpl implements ConnectionRepository {
    @Override
    public List<Connection> findBySourceId(String sourceId) {
        if (sourceId.equals("12345678")) {
            var connection = Connection.Builder.aConnection()
                    .sourceId("12345678")
                    .targetId("87654321")
                    .build();
            return List.of(connection);
        }
        return List.of();
    }
}
