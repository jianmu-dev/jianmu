package dev.jianmu.infrastructure.mybatis.eventbridge;

import dev.jianmu.eventbridge.aggregate.Target;
import dev.jianmu.eventbridge.repository.TargetRepository;
import dev.jianmu.infrastructure.mapper.eventbrdige.TargetMapper;
import dev.jianmu.infrastructure.mapper.eventbrdige.TargetTransformerMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
        this.targetMapper.save(target);
        target.getTransformers().forEach(transformer -> {
            this.targetTransformerMapper.save(target.getId(), transformer, transformer.getClass().getSimpleName());
        });
    }

    @Override
    public void deleteById(String id) {
        this.targetMapper.deleteById(id);
        this.targetTransformerMapper.deleteByTargetId(id);
    }
}
