package dev.jianmu.infrastructure.mybatis.eventbridge;

import dev.jianmu.eventbridge.aggregate.event.SourceEvent;
import dev.jianmu.eventbridge.repository.SourceEventRepository;
import org.springframework.stereotype.Repository;

/**
 * @class: SourceEventRepositoryImpl
 * @description: SourceEventRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-10-22 03:50
 **/
@Repository
public class SourceEventRepositoryImpl implements SourceEventRepository {
    @Override
    public void add(SourceEvent sourceEvent) {
        System.out.println(sourceEvent);
    }
}
