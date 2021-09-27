package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.Bridge;

/**
 * @class: BridgeRepository
 * @description: BridgeRepository
 * @author: Ethan Liu
 * @create: 2021-09-24 15:54
 **/
public interface BridgeRepository {
    void saveOrUpdate(Bridge bridge);

    void deleteById(String id);
}
