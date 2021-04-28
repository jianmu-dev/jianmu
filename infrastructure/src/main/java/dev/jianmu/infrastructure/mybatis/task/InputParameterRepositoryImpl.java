package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.task.aggregate.InputParameter;
import dev.jianmu.task.repository.InputParameterRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @class: InputParameterRepositoryImpl
 * @description: 任务定义输入覆盖参数仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-28 21:40
 **/
@Repository
public class InputParameterRepositoryImpl implements InputParameterRepository {
    @Override
    public void addAll(List<InputParameter> inputParameters) {

    }

    @Override
    public List<InputParameter> findByBusinessIdAndAsyncTaskRef(String businessId, String asyncTaskRef) {
        return null;
    }
}
