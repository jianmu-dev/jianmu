package dev.jianmu.application.service.internal;

import dev.jianmu.application.command.AsyncTaskActivatingCmd;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
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

    public AsyncTaskInstanceInternalApplication(
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            WorkflowInstanceRepository workflowInstanceRepository
    ) {
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
    }

    @Transactional
    public void activate(AsyncTaskActivatingCmd cmd) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(cmd.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        if (!workflowInstance.isRunning()) {
            log.warn("该流程实例已结束，无法创建新任务: {}", cmd.getAsyncTaskRef());
            return;
        }
        var asyncTaskInstance = this.asyncTaskInstanceRepository
                .findByTriggerIdAndTaskRef(cmd.getTriggerId(), cmd.getAsyncTaskRef())
                .orElseThrow(() -> new DataNotFoundException("未找到异步任务示例"));
        asyncTaskInstance.activating();
        this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
    }

    @Transactional
    public void nodeSucceed(String triggerId, String nodeRef, String nextTarget) {
        var asyncTaskInstance = this.asyncTaskInstanceRepository
                .findByTriggerIdAndTaskRef(triggerId, nodeRef)
                .orElseThrow(() -> new DataNotFoundException("未找到异步任务示例"));
        asyncTaskInstance.succeed(nextTarget);
        this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
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
                    this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                });
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
