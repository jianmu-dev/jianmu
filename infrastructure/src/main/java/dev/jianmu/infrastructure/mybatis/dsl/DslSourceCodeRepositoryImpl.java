package dev.jianmu.infrastructure.mybatis.dsl;

import dev.jianmu.dsl.aggregate.DslSourceCode;
import dev.jianmu.dsl.repository.DslSourceCodeRepository;
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
    @Override
    public void add(DslSourceCode dslSourceCode) {

    }

    @Override
    public Optional<DslSourceCode> findByRefAndVersion(String ref, String version) {
        return Optional.empty();
    }
}
