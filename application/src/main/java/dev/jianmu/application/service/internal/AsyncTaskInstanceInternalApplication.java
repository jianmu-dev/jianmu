package dev.jianmu.application.service.internal;

import dev.jianmu.application.command.AsyncTaskActivatingCmd;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.exception.DBException;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
            WorkflowInstanceRepository workflowInstanceRepository) {
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
        try {
            this.asyncTaskInstanceRepository.activateById(asyncTaskInstance, cmd.getVersion());
        } catch (DBException.OptimisticLocking e) {
            log.warn("乐观锁异常，忽略");
        }
    }

    @Transactional
    public void nodeSucceed(String triggerId, String nodeRef, String nextTarget, int version) {
        var asyncTaskInstance = this.asyncTaskInstanceRepository
                .findByTriggerIdAndTaskRef(triggerId, nodeRef)
                .orElseThrow(() -> new DataNotFoundException("未找到异步任务示例"));
        asyncTaskInstance.succeed(nextTarget);
        try {
            this.asyncTaskInstanceRepository.succeedById(asyncTaskInstance, version);
        } catch (DBException.OptimisticLocking e) {
            log.warn("乐观锁异常，忽略");
        }
    }

    // 发布全部任务终止事件
    @Transactional
    public void terminateByTriggerId(String triggerId) {
        // 终止同一流程实例中所有运行中的任务
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByTriggerId(triggerId);
        asyncTaskInstances.stream()
                .filter(asyncTaskInstance -> !asyncTaskInstance.getAsyncTaskType().equalsIgnoreCase("end"))
                .filter(asyncTaskInstance -> asyncTaskInstance.getStatus() == TaskStatus.WAITING)
                .forEach(asyncTaskInstance -> {
                    asyncTaskInstance.terminate();
                    log.info("终止待执行任务: " + asyncTaskInstance.getAsyncTaskRef());
                    this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                });
        asyncTaskInstances.stream()
                .filter(asyncTaskInstance -> !asyncTaskInstance.getAsyncTaskType().equalsIgnoreCase("end"))
                .filter(asyncTaskInstance -> asyncTaskInstance.getStatus() == TaskStatus.RUNNING)
                .forEach(asyncTaskInstance -> {
                    asyncTaskInstance.terminate();
                    log.info("终止运行中任务: " + asyncTaskInstance.getAsyncTaskRef());
                    this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                });
        asyncTaskInstances.stream()
                .filter(asyncTaskInstance -> asyncTaskInstance.getStatus() == TaskStatus.SUSPENDED)
                .forEach(asyncTaskInstance -> {
                    asyncTaskInstance.fail();
                    log.info("终止挂起任务: " + asyncTaskInstance.getAsyncTaskRef());
                    this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                });
    }

    // 任务已启动命令
    @Transactional
    public void waiting(String asyncTaskInstanceId) {
        this.asyncTaskInstanceRepository.findById(asyncTaskInstanceId)
                .ifPresent(asyncTaskInstance -> {
                    asyncTaskInstance.waiting();
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

    // 任务重试
    @Transactional
    public void retry(String instanceId, String taskRef) {
        var workflowInstance = this.workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例: " + instanceId));
        MDC.put("triggerId", workflowInstance.getTriggerId());
        var asyncTaskInstance = this.asyncTaskInstanceRepository.findByTriggerIdAndTaskRef(workflowInstance.getTriggerId(), taskRef)
                .orElseThrow(() -> new DataNotFoundException("未找到该节点实例: " + taskRef));
        var version = asyncTaskInstance.getVersion();
        asyncTaskInstance.retry();
        this.asyncTaskInstanceRepository.retryById(asyncTaskInstance, version);
    }

    // 任务忽略
    @Transactional
    public void ignore(String instanceId, String taskRef) {
        var workflowInstance = this.workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例: " + instanceId));
        MDC.put("triggerId", workflowInstance.getTriggerId());
        var asyncTaskInstance = this.asyncTaskInstanceRepository.findByTriggerIdAndTaskRef(workflowInstance.getTriggerId(), taskRef)
                .orElseThrow(() -> new DataNotFoundException("未找到该节点实例: " + taskRef));
        asyncTaskInstance.doIgnore();
        this.asyncTaskInstanceRepository.ignoreById(asyncTaskInstance);
    }

    // 任务已失败命令
    @Transactional
    public void stop(String triggerId, String asyncTaskInstanceId) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例: " + triggerId));
        MDC.put("triggerId", workflowInstance.getTriggerId());
        if (!workflowInstance.isRunning()) {
            log.info("流程已终止，任务无需挂起");
            this.asyncTaskInstanceRepository.findById(asyncTaskInstanceId)
                    .ifPresent(asyncTaskInstance -> {
                        asyncTaskInstance.fail();
                        this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                    });
            return;
        }
        this.asyncTaskInstanceRepository.findById(asyncTaskInstanceId)
                .ifPresent(asyncTaskInstance -> {
                    asyncTaskInstance.stop();
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
