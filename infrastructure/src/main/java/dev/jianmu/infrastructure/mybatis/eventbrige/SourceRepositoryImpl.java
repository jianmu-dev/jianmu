package dev.jianmu.infrastructure.mybatis.eventbrige;

import dev.jianmu.eventbridge.aggregate.Source;
import dev.jianmu.eventbridge.repository.SourceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class: SourceRepositoryImpl
 * @description: SourceRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-08-14 19:37
 **/
@Repository
public class SourceRepositoryImpl implements SourceRepository {
    @Override
    public Optional<Source> findById(String id) {
        if (id.equals("12345678")) {
            var source = Source.Builder.aSource()
                    .name("source1")
                    .type(Source.Type.WEBHOOK)
                    .build();
            source.setId("12345678");
            return Optional.of(source);
        }
        return Optional.empty();
    }

    @Override
    public List<Source> findByType(Source.Type type) {
        return null;
    }

    @Override
    public List<Source> findAll() {
        return null;
    }
}
