package dev.jianmu.task.repository;

import dev.jianmu.task.aggregate.InputParameter;

import java.util.List;

/**
 * @class: InputParameterRepository
 * @description: 任务定义输入覆盖参数仓储
 * @author: Ethan Liu
 * @create: 2021-04-28 16:18
 **/
public interface InputParameterRepository {
    void addAll(List<InputParameter> inputParameters);

    List<InputParameter> findByBusinessIdAndAsyncTaskRef(String businessId, String asyncTaskRef);
}
