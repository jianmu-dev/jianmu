package dev.jianmu.application.service;

import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @class TaskInstanceApplication
 * @description 任务实例门面类
 * @author Ethan Liu
 * @create 2021-03-25 20:33
*/
@Service
public class TaskInstanceApplication {
    private final TaskInstanceRepository taskInstanceRepository;
    private final InstanceParameterRepository instanceParameterRepository;

    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            InstanceParameterRepository instanceParameterRepository
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.instanceParameterRepository = instanceParameterRepository;
    }

    public List<InstanceParameter> findParameters(String instanceId) {
        return this.instanceParameterRepository.findByInstanceId(instanceId);
    }

//    public List<TaskInstance> findByBusinessId(String businessId) {
//        return this.taskInstanceRepository.findByBusinessId(businessId);
//    }

    public Optional<TaskInstance> findById(String instanceId) {
        return this.taskInstanceRepository.findById(instanceId);
    }
}
