package dev.jianmu.application.service.internal;

import dev.jianmu.application.command.WorkflowStartCmd;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ethan Liu
 * @class WorkflowInstanceInternalApplication
 * @description WorkflowInstanceInternalApplication
 * @create 2021-10-21 15:35
 */
@Service
@Slf4j
public class WorkflowInstanceInternalApplication {
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowInstanceDomainService workflowInstanceDomainService;

    public WorkflowInstanceInternalApplication(
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            WorkflowInstanceDomainService workflowInstanceDomainService
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowInstanceDomainService = workflowInstanceDomainService;
    }

    // 创建并启动流程
    @Transactional
    public void createAndStart(WorkflowStartCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 检查是否存在运行中的流程
        int i = this.workflowInstanceRepository
                .findByRefAndVersionAndStatus(workflow.getRef(), workflow.getVersion(), ProcessStatus.RUNNING)
                .size();
        if (i > 0) {
            throw new RuntimeException("该流程运行中");
        }
        // 查询serialNo
        AtomicInteger serialNo = new AtomicInteger(1);
        this.workflowInstanceRepository.findByRefAndSerialNoMax(workflow.getRef())
                .ifPresent(workflowInstance -> serialNo.set(workflowInstance.getSerialNo() + 1));
        // 创建新的流程实例
        WorkflowInstance workflowInstance = workflowInstanceDomainService.create(cmd.getTriggerId(), cmd.getTriggerType(), serialNo.get(), workflow);
        workflowInstance.start();
        this.workflowInstanceRepository.add(workflowInstance);
    }

    // 流程正常结束
    @Transactional
    public void end(String triggerId) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        workflowInstance.end();
        this.workflowInstanceRepository.save(workflowInstance);
    }

    // 终止流程
    @Transactional
    public void stop(String instanceId) {
        var workflowInstance = this.workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        // 终止流程
        MDC.put("triggerId", workflowInstance.getTriggerId());
        workflowInstance.terminate();
        this.workflowInstanceRepository.save(workflowInstance);
    }

    @Transactional
    public void statusCheck(String triggerId) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        workflowInstance.statusCheck();
        this.workflowInstanceRepository.save(workflowInstance);
    }
}
