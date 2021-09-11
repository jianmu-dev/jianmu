package dev.jianmu.worker.repository;

import dev.jianmu.worker.aggregate.Worker;

import java.util.Optional;

/**
 * @class: WorkerRepository
 * @description: Worker仓储接口
 * @author: Ethan Liu
 * @create: 2021-04-02 12:28
 **/
public interface WorkerRepository {
    void add(Worker worker);

    void delete(Worker worker);

    void updateStatus(Worker worker);

    Optional<Worker> findById(String workerId);
}
