package dev.jianmu.infrastructure.mybatis.trigger;

import dev.jianmu.infrastructure.mapper.trigger.TriggerMapper;
import dev.jianmu.infrastructure.mapper.trigger.TriggerParameterMapper;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.repository.TriggerRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @class: TriggerRepositoryImpl
 * @description: 触发器仓储接口实现
 * @author: Ethan Liu
 * @create: 2021-04-08 18:29
 **/
@Repository
public class TriggerRepositoryImpl implements TriggerRepository {
    private final TriggerMapper triggerMapper;
    private final TriggerParameterMapper triggerParameterMapper;

    @Inject
    public TriggerRepositoryImpl(TriggerMapper triggerMapper, TriggerParameterMapper triggerParameterMapper) {
        this.triggerMapper = triggerMapper;
        this.triggerParameterMapper = triggerParameterMapper;
    }

    @Override
    public void add(Trigger trigger) {
        this.triggerMapper.add(trigger);
        if (!trigger.getParameters().isEmpty()) {
            this.triggerParameterMapper.addAll(trigger.getId(), trigger.getParameters());
        }
    }

    @Override
    public void delete(Trigger trigger) {
        this.triggerMapper.delete(trigger);
        this.triggerParameterMapper.deleteByTriggerId(trigger.getId());
    }

    @Override
    public Optional<Trigger> findById(String triggerId) {
        var triggerOptional = this.triggerMapper.findById(triggerId);
        var parameters = this.triggerParameterMapper.findByTriggerId(triggerId);
        triggerOptional.ifPresent(trigger -> trigger.setTriggerParameters(parameters));
        return triggerOptional;
    }

    @Override
    public List<Trigger> findAll() {
        return this.triggerMapper.findAll();
    }
}
