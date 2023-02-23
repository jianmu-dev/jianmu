package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.InstanceParameterMapper;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.repository.InstanceParameterRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Ethan Liu
 * @class InstanceParameterRepositoryImpl
 * @description 任务实例参数仓储实现
 * @create 2021-04-28 18:48
 */
@Repository
public class InstanceParameterRepositoryImpl implements InstanceParameterRepository {
    private final InstanceParameterMapper instanceParameterMapper;

    public InstanceParameterRepositoryImpl(InstanceParameterMapper instanceParameterMapper) {
        this.instanceParameterMapper = instanceParameterMapper;
    }

    @Override
    public void addAll(Set<InstanceParameter> instanceParameters) {
        if (!instanceParameters.isEmpty()) {
            this.instanceParameterMapper.addAll(instanceParameters);
        }
    }

    @Override
    public List<InstanceParameter> findByInstanceId(String instanceId) {
        return this.instanceParameterMapper.findByInstanceId(instanceId);
    }

    @Override
    public List<InstanceParameter> findByInstanceIdAndType(String instanceId, InstanceParameter.Type type) {
        return this.instanceParameterMapper.findByInstanceIdAndType(instanceId, type);
    }

    @Override
    public List<InstanceParameter> findLastOutputParamByTriggerId(String triggerId) {
        return this.instanceParameterMapper.findLastOutputParamByTriggerId(triggerId);
    }

    @Override
    public void deleteByTriggerId(String triggerId) {
        this.instanceParameterMapper.deleteByTriggerId(triggerId);
    }
}
