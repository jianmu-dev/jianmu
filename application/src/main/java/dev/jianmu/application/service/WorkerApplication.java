package dev.jianmu.application.service;

import dev.jianmu.infrastructure.docker.DockerWorker;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.Worker;
import dev.jianmu.task.repository.WorkerRepository;
import dev.jianmu.task.service.WorkerDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;

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
    private final TaskInstanceApplication taskInstanceApplication;
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final StorageService storageService;
    private final DockerWorker dockerWorker;
    private final WorkerDomainService workerDomainService;

    @Inject
    public WorkerApplication(
            WorkerRepository workerRepository,
            TaskInstanceApplication taskInstanceApplication,
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            StorageService storageService,
            DockerWorker dockerWorker, WorkerDomainService workerDomainService) {
        this.workerRepository = workerRepository;
        this.taskInstanceApplication = taskInstanceApplication;
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.storageService = storageService;
        this.dockerWorker = dockerWorker;
        this.workerDomainService = workerDomainService;
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

    public void createVolume(String volumeName) {
        this.dockerWorker.createVolume(volumeName);
    }

    public void deleteVolume(String volumeName) {
        this.dockerWorker.deleteVolume(volumeName);
    }

    public void runTask(TaskInstance taskInstance) {
        // 创建DockerTask
        var environmentMap = this.taskInstanceApplication.getEnvironmentMap(taskInstance);
        var dockerTask = this.workerDomainService.createDockerTask(taskInstance, environmentMap);
        // 创建logWriter
        var logWriter = storageService.writeLog(taskInstance.getId());
        // 执行任务
        this.dockerWorker.runTask(dockerTask, logWriter);
    }
}
