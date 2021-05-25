package dev.jianmu.project.repository;

import dev.jianmu.project.aggregate.CronTrigger;

import java.util.List;
import java.util.Optional;

/**
 * @class: CronTriggerRepository
 * @description: CronTriggerRepository
 * @author: Ethan Liu
 * @create: 2021-05-25 11:17
 **/
public interface CronTriggerRepository {
    void add(CronTrigger cronTrigger);

    void deleteByProjectId(String projectId);

    void deleteById(String id);

    Optional<CronTrigger> findById(String id);

    List<CronTrigger> findByProjectId(String projectId);
}
