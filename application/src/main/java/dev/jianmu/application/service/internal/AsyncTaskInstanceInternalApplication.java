package dev.jianmu.application.service.internal;

import dev.jianmu.application.command.AsyncTaskActivatingCmd;
import dev.jianmu.application.command.SkipNodeCmd;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Ethan Liu
 * @class AsyncTaskInstanceInternalApplication
 * @description AsyncTaskInstanceInternalApplication
 * @create 2022-01-02 00:28
 */
@Service
@Slf4j
public class AsyncTaskInstanceInternalApplication {
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowRepository workflowRepository;

    public AsyncTaskInstanceInternalApplication(
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            WorkflowRepository workflowRepository
    ) {
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowRepository = workflowRepository;
    }

    @Transactional
    public void create(AsyncTaskActivatingCmd cmd) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(cmd.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        if (!workflowInstance.isRunning()) {
            log.warn("该流程实例已结束，无法创建新任务");
            return;
        }
        var workflow = this.workflowRepository.findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程定义"));
        var node = workflow.findNode(cmd.getAsyncTaskRef());
        var asyncTaskInstance = AsyncTaskInstance.Builder
                .anAsyncTaskInstance()
                .workflowInstanceId(workflowInstance.getId())
                .triggerId(cmd.getTriggerId())
                .workflowRef(cmd.getWorkflowRef())
                .workflowVersion(cmd.getWorkflowVersion())
                .name(node.getName())
                .description(node.getDescription())
                .asyncTaskRef(cmd.getAsyncTaskRef())
                .asyncTaskType(cmd.getAsyncTaskType())
                .build();
        asyncTaskInstance.activating();
        this.asyncTaskInstanceRepository.add(asyncTaskInstance);
    }

    @Transactional
    public void skip(SkipNodeCmd cmd) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(cmd.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var workflow = this.workflowRepository.findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程定义"));
        var node = workflow.findNode(cmd.getNodeRef());
        var asyncTaskInstance = AsyncTaskInstance.Builder
                .anAsyncTaskInstance()
                .workflowInstanceId(workflowInstance.getId())
                .triggerId(cmd.getTriggerId())
                .workflowRef(cmd.getWorkflowRef())
                .workflowVersion(cmd.getWorkflowVersion())
                .name(node.getName())
                .description(node.getDescription())
                .asyncTaskRef(node.getRef())
                .asyncTaskType(node.getType())
                .build();
        asyncTaskInstance.skip();
        this.asyncTaskInstanceRepository.add(asyncTaskInstance);
    }

    // 发布全部任务终止事件
    @Transactional
    public void terminateAll(String instanceId) {
        // 终止同一流程实例中所有运行中的任务
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByInstanceId(instanceId);
        asyncTaskInstances.stream()
                .filter(asyncTaskInstance -> asyncTaskInstance.getStatus() == TaskStatus.RUNNING)
                .forEach(asyncTaskInstance -> {
                    asyncTaskInstance.terminate();
                    log.info("terminateNode: " + asyncTaskInstance.getAsyncTaskRef());
                });
        this.asyncTaskInstanceRepository.updateAll(asyncTaskInstances);
    }

    // 任务已启动命令
    @Transactional
    public void run(String asyncTaskInstanceId) {
        this.asyncTaskInstanceRepository.findById(asyncTaskInstanceId)
                .ifPresent(asyncTaskInstance -> {
                    asyncTaskInstance.run();
                    this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                });
    }

    // 任务已失败命令
    @Transactional
    public void fail(String asyncTaskInstanceId) {
        this.asyncTaskInstanceRepository.findById(asyncTaskInstanceId)
                .ifPresent(asyncTaskInstance -> {
                    asyncTaskInstance.fail();
                    this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                });
    }

    // 任务已成功命令
    @Transactional
    public void succeed(String asyncTaskInstanceId) {
        this.asyncTaskInstanceRepository.findById(asyncTaskInstanceId)
                .ifPresent(asyncTaskInstance -> {
                    asyncTaskInstance.succeed();
                    this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                });
    }
}
