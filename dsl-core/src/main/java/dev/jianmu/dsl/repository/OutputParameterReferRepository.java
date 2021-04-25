package dev.jianmu.dsl.repository;

import dev.jianmu.dsl.aggregate.OutputParameterRefer;

import java.util.List;
import java.util.Set;

/**
 * @class: OutputParameterReferRepository
 * @description: 输出参数引用关系仓储
 * @author: Ethan Liu
 * @create: 2021-04-25 14:02
 **/
public interface OutputParameterReferRepository {
    void addAll(Set<OutputParameterRefer> refers);

    List<OutputParameterRefer> findByContextId(String contextId);
}
