package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.repository.InstanceParameterRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @class: InstanceParameterRepositoryImpl
 * @description: 任务实例参数仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-28 18:48
 **/
@Repository
public class InstanceParameterRepositoryImpl implements InstanceParameterRepository {
    @Override
    public void addAll(Set<InstanceParameter> instanceParameters) {

    }
}
