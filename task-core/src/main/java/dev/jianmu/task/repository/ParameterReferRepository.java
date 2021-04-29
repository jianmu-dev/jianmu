package dev.jianmu.task.repository;

import dev.jianmu.task.aggregate.ParameterRefer;

import java.util.List;

/**
 * @class: ParameterReferRepository
 * @description: 参数关联仓储
 * @author: Ethan Liu
 * @create: 2021-04-29 15:09
 **/
public interface ParameterReferRepository {
    void addAll(List<ParameterRefer> parameterRefers);

    List<ParameterRefer> findByRefAndVersionAndTargetTaskRef(String workflowRef, String workflowVersion, String targetTaskRef);
}
