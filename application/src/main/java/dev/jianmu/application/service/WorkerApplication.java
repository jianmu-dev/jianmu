package dev.jianmu.application.service;

import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.task.aggregate.Worker;
import dev.jianmu.task.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class: WorkerApplication
 * @description: 任务执行器门面类
 * @author: Ethan Liu
 * @create: 2021-04-02 12:30
 **/
@Service
@Transactional
public class WorkerApplication {
    private final WorkerRepository workerRepository;
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;

    @Inject
    public WorkerApplication(WorkerRepository workerRepository, ParameterRepository parameterRepository, ParameterDomainService parameterDomainService) {
        this.workerRepository = workerRepository;
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
    }

    public void online(String workerId) {
        Worker worker = this.workerRepository.findById(workerId).orElseThrow(() -> new RuntimeException("未找到该Worker"));
        worker.online();
        this.workerRepository.updateStatus(worker);
    }

    public void offline(String workerId) {
        Worker worker = this.workerRepository.findById(workerId).orElseThrow(() -> new RuntimeException("未找到该Worker"));
        worker.offline();
        this.workerRepository.updateStatus(worker);
    }

    public void add(Worker worker, Map<String, Object> parameterMap) {
        var parameters = this.parameterDomainService.createParameters(parameterMap);
        var workerParameterMap = this.parameterDomainService.createParameterMap(parameters);
        worker.setParameterMap(workerParameterMap);
        this.parameterRepository.addAll(new ArrayList<>(parameters.values()));
        this.workerRepository.add(worker);
    }
}
