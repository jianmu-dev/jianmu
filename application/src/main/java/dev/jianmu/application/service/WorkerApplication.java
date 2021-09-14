package dev.jianmu.application.service;

import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.worker.event.CleanupWorkspaceEvent;
import dev.jianmu.worker.event.CreateWorkspaceEvent;
import dev.jianmu.worker.repository.WorkerRepository;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
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
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final NodeDefApi nodeDefApi;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher publisher;
    private final InstanceParameterRepository instanceParameterRepository;

    public WorkerApplication(
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            NodeDefApi nodeDefApi,
            WorkerRepository workerRepository,
            ApplicationEventPublisher publisher,
            InstanceParameterRepository instanceParameterRepository
    ) {
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.nodeDefApi = nodeDefApi;
        this.workerRepository = workerRepository;
        this.publisher = publisher;
        this.instanceParameterRepository = instanceParameterRepository;
    }

    private Worker findWorker() {
        // 查找符合条件的Worker
        // TODO 暂时全部分配给内置Worker
        var worker = this.workerRepository.findByType(Worker.Type.EMBEDDED);
        return worker;
    }

    public void createWorkspace(String triggerId) {
        var worker = this.findWorker();
        this.publisher.publishEvent(
                CreateWorkspaceEvent.Builder.aCreateWorkspaceEvent()
                        .workerId(worker.getId())
                        .workerType(worker.getType().name())
                        .workspaceName(triggerId)
                        .build()
        );
    }

    public void cleanupWorkspace(String triggerId) {
        var worker = this.findWorker();
        this.publisher.publishEvent(
                CleanupWorkspaceEvent.Builder.aCleanupWorkspaceEvent()
                        .workerId(worker.getId())
                        .workerType(worker.getType().name())
                        .workspaceName(triggerId)
                        .build()
        );
    }

    public void dispatchTask(TaskInstance taskInstance) {
        // 查找节点定义
        var nodeDef = this.nodeDefApi.findByType(taskInstance.getDefKey());
        if (!nodeDef.getWorkerType().equals("DOCKER")) {
            throw new RuntimeException("无法执行此类节点任务: " + nodeDef.getType());
        }
        var worker = this.findWorker();
        // 创建WorkerTask
        var instanceParameters = this.instanceParameterRepository
                .findByInstanceIdAndType(taskInstance.getId(), InstanceParameter.Type.INPUT);
        var parameterMap = this.getParameterMap(instanceParameters);
        var workerTask = WorkerTask.Builder.aWorkerTask()
                .workerId(worker.getId())
                .type(worker.getType())
                .taskInstanceId(taskInstance.getId())
                .businessId(taskInstance.getBusinessId())
                .triggerId(taskInstance.getTriggerId())
                .defKey(taskInstance.getDefKey())
                .resultFile(nodeDef.getResultFile())
                .spec(nodeDef.getSpec())
                .parameterMap(parameterMap)
                .build();
        // 发送给Worker执行
        this.publisher.publishEvent(workerTask);
    }

    private Map<String, String> getParameterMap(List<InstanceParameter> instanceParameters) {
        var parameterMap = instanceParameters.stream()
                .map(instanceParameter -> Map.entry(
                        instanceParameter.getRef(),
                        instanceParameter.getParameterId()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 查询参数值
        var parameters = this.parameterRepository.findByIds(new HashSet<>(parameterMap.values()));
        // 替换实际参数值
        this.parameterDomainService.createParameterMap(parameterMap, parameters);
        return parameterMap;
    }
}
