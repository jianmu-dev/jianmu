package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.WorkerMapper;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.repository.WorkerRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class WorkerRepositoryImpl
 * @description Worker仓储接口实现
 * @create 2021-04-02 12:37
 */
@Repository
public class WorkerRepositoryImpl implements WorkerRepository {
    private final WorkerMapper workerMapper;

    public WorkerRepositoryImpl(WorkerMapper workerMapper) {
        this.workerMapper = workerMapper;
    }

    @Override
    public void add(Worker worker) {
        this.workerMapper.add(worker);
    }

    @Override
    public void delete(Worker worker) {
        this.workerMapper.delete(worker);
    }

    @Override
    public void updateStatus(Worker worker) {
        this.workerMapper.updateStatus(worker);
    }

    @Override
    public void updateTag(Worker worker) {
        this.workerMapper.updateTag(worker);
    }

    @Override
    public List<Worker> findAll() {
        return this.workerMapper.findAll();
    }

    @Override
    public Optional<Worker> findById(String workerId) {
        return this.workerMapper.findById(workerId);
    }

    @Override
    public Worker findByType(Worker.Type type) {
        return Worker.Builder.aWorker()
                .id("embedded-worker-1")
                .name("Embedded-Worker")
                .type(Worker.Type.EMBEDDED)
                .status(Worker.Status.ONLINE)
                .build();
    }

    @Override
    public List<Worker> findByTypeInAndCreatedTimeLessThan(List<Worker.Type> types, LocalDateTime createdTime) {
        return this.workerMapper.findByTypeInAndCreatedTimeLessThan(types, createdTime);
    }

    @Override
    public List<Worker> findByTypeInAndTagAndCreatedTimeLessThan(List<Worker.Type> types, List<String> tags, LocalDateTime createdTime) {
        return this.workerMapper.findByTypeInAndTagAndCreatedTimeLessThan(types, tags, createdTime);
    }
}
