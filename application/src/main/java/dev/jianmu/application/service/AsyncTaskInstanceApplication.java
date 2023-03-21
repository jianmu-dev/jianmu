package dev.jianmu.application.service;

import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class AsyncTaskInstanceApplication
 * @description AsyncTaskInstanceApplication
 * @create 2022-01-03 10:51
 */
@Service
@Slf4j
public class AsyncTaskInstanceApplication {
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;

    public AsyncTaskInstanceApplication(AsyncTaskInstanceRepository asyncTaskInstanceRepository) {
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
    }

    public List<AsyncTaskInstance> findByTriggerId(String triggerId) {
        return this.asyncTaskInstanceRepository.findByTriggerId(triggerId);
    }

    public Optional<AsyncTaskInstance> findById(String asyncTaskId) {
        return this.asyncTaskInstanceRepository.findById(asyncTaskId);
    }
}
