package dev.jianmu.application.service;

import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class WorkerApplication {
    private final WorkerRepository workerRepository;
    private final TaskInstanceRepository taskInstanceRepository;

    public WorkerApplication(WorkerRepository workerRepository, TaskInstanceRepository taskInstanceRepository) {
        this.workerRepository = workerRepository;
        this.taskInstanceRepository = taskInstanceRepository;
    }

    public List<Worker> findAll() {
        return this.workerRepository.findAll();
    }

    @Transactional
    public void delete(String workerId) {
        this.workerRepository.findById(workerId).ifPresent(worker -> {
            this.workerRepository.delete(worker);
            this.taskInstanceRepository.deleteByWorkerId(worker.getId());
        });
    }
}
