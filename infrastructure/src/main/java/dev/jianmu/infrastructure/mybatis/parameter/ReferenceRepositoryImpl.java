package dev.jianmu.infrastructure.mybatis.parameter;

import dev.jianmu.infrastructure.mapper.parameter.ReferenceMapper;
import dev.jianmu.parameter.aggregate.Reference;
import dev.jianmu.parameter.repository.ReferenceRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @class: ReferenceRepositoryImpl
 * @description: 参数引用仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-10 09:54
 **/
@Repository
public class ReferenceRepositoryImpl implements ReferenceRepository {
    private final ReferenceMapper referenceMapper;

    @Inject
    public ReferenceRepositoryImpl(ReferenceMapper referenceMapper) {
        this.referenceMapper = referenceMapper;
    }

    @Override
    public void addAll(List<Reference> references) {
        this.referenceMapper.addAll(references);
    }

    @Override
    public List<Reference> findByContextIds(Set<String> contextIds) {
        if (contextIds.isEmpty()) {
            return List.of();
        }
        return this.referenceMapper.findByContextIds(contextIds);
    }

    @Override
    public List<Reference> findByContextId(String contextId) {
        return this.referenceMapper.findByContextId(contextId);
    }
}
