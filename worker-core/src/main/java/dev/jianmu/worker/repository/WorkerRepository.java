package dev.jianmu.worker.repository;

import dev.jianmu.worker.aggregate.Worker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class WorkerRepository
 * @description Worker仓储接口
 * @create 2021-04-02 12:28
 */
public interface WorkerRepository {
    void add(Worker worker);

    void delete(Worker worker);

    void updateStatus(Worker worker);

    void updateTag(Worker worker);

    List<Worker> findAll();

    Optional<Worker> findById(String workerId);

    Worker findByType(Worker.Type type);

    List<Worker> findByTypeInAndCreatedTimeLessThan(List<Worker.Type> types, LocalDateTime createdTime);

    List<Worker> findByTypeInAndTagAndCreatedTimeLessThan(List<Worker.Type> types, List<String> tags, LocalDateTime createdTime);
}
