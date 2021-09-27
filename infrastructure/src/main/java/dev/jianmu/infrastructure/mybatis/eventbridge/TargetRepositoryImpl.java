package dev.jianmu.infrastructure.mybatis.eventbridge;

import dev.jianmu.eventbridge.aggregate.Target;
import dev.jianmu.eventbridge.repository.TargetRepository;
import dev.jianmu.infrastructure.mapper.eventbrdige.TargetMapper;
import dev.jianmu.infrastructure.mapper.eventbrdige.TargetTransformerMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * @class: TargetRepositoryImpl
 * @description: TargetRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-08-14 19:38
 **/
@Repository
public class TargetRepositoryImpl implements TargetRepository {
    private final TargetMapper targetMapper;
    private final TargetTransformerMapper targetTransformerMapper;

    public TargetRepositoryImpl(TargetMapper targetMapper, TargetTransformerMapper targetTransformerMapper) {
        this.targetMapper = targetMapper;
        this.targetTransformerMapper = targetTransformerMapper;
    }

    @Override
    public Optional<Target> findById(String id) {
        return this.targetMapper.findById(id).map(target -> {
            var transformers = this.targetTransformerMapper.findByTargetId(target.getRef());
            target.setTransformers(transformers);
            return target;
        });
    }

    @Override
    public Optional<Target> findByRef(String ref) {
        return this.targetMapper.findByRef(ref).map(target -> {
            var transformers = this.targetTransformerMapper.findByTargetId(target.getId());
            target.setTransformers(transformers);
            return target;
        });
    }

    @Override
    public Optional<Target> findByDestinationId(String destinationId) {
        return this.targetMapper.findByDestinationId(destinationId).map(target -> {
            var transformers = this.targetTransformerMapper.findByTargetId(target.getId());
            target.setTransformers(transformers);
            return target;
        });
    }

    @Override
    public void save(Target target) {
        this.targetMapper.saveOrUpdate(target);
        target.getTransformers().forEach(transformer -> {
            this.targetTransformerMapper.deleteByTargetId(target.getId());
            this.targetTransformerMapper.save(target.getBridgeId(), target.getId(), transformer, transformer.getClass().getSimpleName());
        });
    }

    @Override
    public void saveOrUpdateList(Set<Target> targets) {
        targets.forEach(this::save);
    }

    @Override
    public void deleteById(String id) {
        this.targetMapper.deleteById(id);
        this.targetTransformerMapper.deleteByTargetId(id);
    }

    @Override
    public void deleteByBridgeId(String bridgeId) {
        this.targetMapper.deleteByBridgeId(bridgeId);
        this.targetTransformerMapper.deleteByBridgeId(bridgeId);
    }
}
