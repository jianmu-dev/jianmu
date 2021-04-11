package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.WorkerMapper;
import dev.jianmu.infrastructure.mapper.task.WorkerParameterMapper;
import dev.jianmu.task.aggregate.Worker;
import dev.jianmu.task.repository.WorkerRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @class: WorkerRepositoryImpl
 * @description: Worker仓储接口实现
 * @author: Ethan Liu
 * @create: 2021-04-02 12:37
 **/
@Repository
public class WorkerRepositoryImpl implements WorkerRepository {
    private final WorkerMapper workerMapper;
    private final WorkerParameterMapper workerParameterMapper;

    @Inject
    public WorkerRepositoryImpl(WorkerMapper workerMapper, WorkerParameterMapper workerParameterMapper) {
        this.workerMapper = workerMapper;
        this.workerParameterMapper = workerParameterMapper;
    }

    @Override
    public void add(Worker worker) {
        this.workerMapper.add(worker);
        this.workerParameterMapper.addAll(worker.getId(), worker.getParameterMap());
    }

    @Override
    public void delete(Worker worker) {
        this.workerMapper.delete(worker);
        this.workerParameterMapper.deleteByWorkerId(worker.getId());
    }

    @Override
    public void updateStatus(Worker worker) {
        this.workerMapper.updateStatus(worker);
    }

    @Override
    public Optional<Worker> findById(String workerId) {
        var workerOptional = this.workerMapper.findById(workerId);
        var workerParameters = this.workerParameterMapper.findByWorkerId(workerId);
        var parameterMap = workerParameters.stream()
                .map(workerParameterDO -> Map.entry(workerParameterDO.getParameterKey(), workerParameterDO.getParameterValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        workerOptional.ifPresent(worker -> worker.setParameterMap(parameterMap));
        return workerOptional;
    }
}
