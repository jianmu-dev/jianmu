package dev.jianmu.infrastructure.mybatis.eventbrige;

import dev.jianmu.eventbridge.aggregate.TargetEvent;
import dev.jianmu.eventbridge.repository.TargetEventRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

/**
 * @class: TargetEventRepositoryImpl
 * @description: TargetEventRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-08-14 19:39
 **/
@Repository
public class TargetEventRepositoryImpl implements TargetEventRepository {
    @Override
    public Optional<TargetEvent> findById(String id) {
        return Optional.empty();
    }

    @Override
    public void save(TargetEvent targetEvent) {
        System.out.println(targetEvent.getId());
        System.out.println(targetEvent.getTargetId());
        System.out.println(targetEvent.getSourceId());
        System.out.println(targetEvent.getPayload().getBody());
        System.out.println(targetEvent.getPayload().getPath());
        targetEvent.getPayload().getHeader().forEach((key, value) -> {
            System.out.println(key);
            System.out.println(value);
        });
        targetEvent.getPayload().getQuery().forEach((key, value) -> {
            System.out.println(key);
            System.out.println(Arrays.toString(value));
        });
        targetEvent.getEventParameters().forEach(eventParameter -> {
            System.out.println(eventParameter.getName());
            System.out.println(eventParameter.getType());
            System.out.println(eventParameter.getParameterId());
        });
    }
}
