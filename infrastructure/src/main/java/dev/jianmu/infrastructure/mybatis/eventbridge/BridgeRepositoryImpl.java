package dev.jianmu.infrastructure.mybatis.eventbridge;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.eventbridge.aggregate.Bridge;
import dev.jianmu.eventbridge.repository.BridgeRepository;
import dev.jianmu.infrastructure.mapper.eventbrdige.BridgeMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Override
    public void deleteById(String id) {
        this.bridgeMapper.deleteById(id);
    }

    @Override
    public Optional<Bridge> findById(String id) {
        return this.bridgeMapper.findById(id);
    }

    public PageInfo<Bridge> findAllPage(int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(this.bridgeMapper::findAll);
    }
}
