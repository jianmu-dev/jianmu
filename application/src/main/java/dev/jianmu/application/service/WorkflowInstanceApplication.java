package dev.jianmu.application.service;

import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.mybatis.workflow.WorkflowInstanceRepositoryImpl;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 流程实例门面类
 * @create 2021-01-22 14:50
 */
@Service
public class WorkflowInstanceApplication {
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final GlobalProperties globalProperties;

    public WorkflowInstanceApplication(
            WorkflowInstanceRepositoryImpl workflowInstanceRepository,
            GlobalProperties globalProperties
    ) {
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.globalProperties = globalProperties;
    }

    public Optional<WorkflowInstance> findById(String id) {
        return this.workflowInstanceRepository.findById(id);
    }

    public List<WorkflowInstance> findByWorkflowRef(String workflowRef) {
        return this.workflowInstanceRepository.findByWorkflowRefLimit(workflowRef, globalProperties.getGlobal().getRecord().getMax());
    }

    public Optional<WorkflowInstance> findByRefAndSerialNoMax(String workflowRef) {
        return this.workflowInstanceRepository.findByRefAndSerialNoMax(workflowRef);
    }
}
