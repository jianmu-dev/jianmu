package dev.jianmu.infrastructure.mybatis.eventbridge;

import dev.jianmu.eventbridge.aggregate.Bridge;
import dev.jianmu.eventbridge.repository.BridgeRepository;
import dev.jianmu.infrastructure.mapper.eventbrdige.BridgeMapper;
import org.springframework.stereotype.Repository;

/**
 * @class: BridgeRepositoryImpl
 * @description: BridgeRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-09-27 09:35
 **/
@Repository
public class BridgeRepositoryImpl implements BridgeRepository {
    private final BridgeMapper bridgeMapper;

    public BridgeRepositoryImpl(BridgeMapper bridgeMapper) {
        this.bridgeMapper = bridgeMapper;
    }

    @Override
    public void saveOrUpdate(Bridge bridge) {
        this.bridgeMapper.saveOrUpdate(bridge);
    }
}
