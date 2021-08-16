package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.Source;

import java.util.List;
import java.util.Optional;

/**
 * @class: SourceRepository
 * @description: SourceRepository
 * @author: Ethan Liu
 * @create: 2021-08-11 15:52
 **/
public interface SourceRepository {
    Optional<Source> findById(String id);

    List<Source> findByType(Source.Type type);

    List<Source> findAll();
}
