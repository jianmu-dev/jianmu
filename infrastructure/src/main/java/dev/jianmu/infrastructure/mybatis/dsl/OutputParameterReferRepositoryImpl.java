package dev.jianmu.infrastructure.mybatis.dsl;

import dev.jianmu.dsl.aggregate.OutputParameterRefer;
import dev.jianmu.dsl.repository.OutputParameterReferRepository;
import dev.jianmu.infrastructure.mapper.dsl.OutputParameterReferMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @class: OutputParameterReferRepositoryImpl
 * @description: 输出参数引用关系仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-25 14:10
 **/
@Repository
public class OutputParameterReferRepositoryImpl implements OutputParameterReferRepository {
    private final OutputParameterReferMapper outputParameterReferMapper;

    public OutputParameterReferRepositoryImpl(OutputParameterReferMapper outputParameterReferMapper) {
        this.outputParameterReferMapper = outputParameterReferMapper;
    }

    @Override
    public void addAll(Set<OutputParameterRefer> refers) {
        refers.forEach(System.out::println);
        this.outputParameterReferMapper.addAll(refers);
    }

    @Override
    public List<OutputParameterRefer> findByContextId(String contextId) {
        return this.outputParameterReferMapper.findByContextId(contextId);
    }
}
