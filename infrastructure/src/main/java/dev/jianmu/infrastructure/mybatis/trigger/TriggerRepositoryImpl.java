package dev.jianmu.infrastructure.mybatis.trigger;

import dev.jianmu.infrastructure.mapper.trigger.TriggerMapper;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.repository.TriggerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class TriggerRepositoryImpl
 * @description TriggerRepositoryImpl
 * @author Ethan Liu
 * @create 2021-11-10 15:28
 */
@Repository
public class TriggerRepositoryImpl implements TriggerRepository {
    private final TriggerMapper triggerMapper;

    public TriggerRepositoryImpl(TriggerMapper triggerMapper) {
        this.triggerMapper = triggerMapper;
    }

    @Override
    public void add(Trigger trigger) {
        this.triggerMapper.add(trigger);
    }

    @Override
    public void updateById(Trigger trigger) {
        this.triggerMapper.updateById(trigger);
    }

    @Override
    public void deleteById(String id) {
        this.triggerMapper.deleteById(id);
    }

    @Override
    public Optional<Trigger> findByProjectId(String projectId) {
        return this.triggerMapper.findByProjectId(projectId);
    }

    @Override
    public Optional<Trigger> findByTriggerId(String triggerId) {
        return this.triggerMapper.findByTriggerId(triggerId);
    }

    @Override
    public List<Trigger> findCronTriggerAll() {
        return this.triggerMapper.findAllByType(Trigger.Type.CRON);
    }

    @Override
    public Optional<Trigger> findByRef(String ref) {
        return this.triggerMapper.findByRef(ref);
    }
}
