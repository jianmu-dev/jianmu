package dev.jianmu.infrastructure.mybatis.eventbrige;

import dev.jianmu.eventbridge.aggregate.Target;
import dev.jianmu.eventbridge.repository.TargetRepository;
import dev.jianmu.infrastructure.eventbridge.BodyTransformer;
import dev.jianmu.infrastructure.eventbridge.HeaderTransformer;
import dev.jianmu.infrastructure.eventbridge.QueryTransformer;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * @class: TargetRepositoryImpl
 * @description: TargetRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-08-14 19:38
 **/
@Repository
public class TargetRepositoryImpl implements TargetRepository {
    @Override
    public Optional<Target> findById(String id) {
        if (id.equals("87654321")) {
            var bodyTransformer = BodyTransformer.Builder.aBodyTransformer()
                    .variableName("testVar1")
                    .variableType("STRING")
                    .expression("$.id")
                    .build();
            var headerTransformer = HeaderTransformer.Builder.aHeaderTransformer()
                    .variableName("testVar2")
                    .variableType("STRING")
                    .expression("aaa")
                    .build();
            var queryTransformer = QueryTransformer.Builder.aQueryTransformer()
                    .variableName("testVar3")
                    .variableType("STRING")
                    .expression("abc")
                    .build();
            var target = Target.Builder.aTarget()
                    .name("target1")
                    .type(Target.Type.WORKFLOW)
                    .destinationId("project_123")
                    .transformers(Set.of(bodyTransformer, headerTransformer, queryTransformer))
                    .build();
            target.setId("87654321");
            return Optional.of(target);
        }
        return Optional.empty();
    }
}
