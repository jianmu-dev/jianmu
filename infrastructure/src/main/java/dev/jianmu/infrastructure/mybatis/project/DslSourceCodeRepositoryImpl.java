package dev.jianmu.infrastructure.mybatis.project;

import dev.jianmu.infrastructure.mapper.project.DslSourceCodeMapper;
import dev.jianmu.project.aggregate.DslSourceCode;
import dev.jianmu.project.repository.DslSourceCodeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: DslSourceCodeRepositoryImpl
 * @description: DslSourceCodeRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-04-23 23:02
 **/
@Repository
public class DslSourceCodeRepositoryImpl implements DslSourceCodeRepository {
    private final DslSourceCodeMapper dslSourceCodeMapper;

    public DslSourceCodeRepositoryImpl(DslSourceCodeMapper dslSourceCodeMapper) {
        this.dslSourceCodeMapper = dslSourceCodeMapper;
    }

    @Override
    public void add(DslSourceCode dslSourceCode) {
        this.dslSourceCodeMapper.add(dslSourceCode);
    }

    @Override
    public void deleteByProjectId(String projectId) {
        this.dslSourceCodeMapper.deleteByProjectId(projectId);
    }

    @Override
    public Optional<DslSourceCode> findByRefAndVersion(String ref, String version) {
        return this.dslSourceCodeMapper.findByRefAndVersion(ref, version);
    }
}
