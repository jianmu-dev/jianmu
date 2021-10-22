package dev.jianmu.application.service;

import dev.jianmu.infrastructure.mybatis.workflow.WorkflowInstanceRepositoryImpl;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @program: workflow
 * @description: 流程实例门面类
 * @author: Ethan Liu
 * @create: 2021-01-22 14:50
 **/
@Service
public class WorkflowInstanceApplication {
    private final WorkflowInstanceRepositoryImpl workflowInstanceRepository;

    public WorkflowInstanceApplication(WorkflowInstanceRepositoryImpl workflowInstanceRepository) {
        this.workflowInstanceRepository = workflowInstanceRepository;
    }

    public Optional<WorkflowInstance> findById(String id) {
        return this.workflowInstanceRepository.findById(id);
    }

    public List<WorkflowInstance> findByWorkflowRef(String workflowRef) {
        return this.workflowInstanceRepository.findByWorkflowRef(workflowRef);
    }

    public Optional<WorkflowInstance> findByRefAndSerialNoMax(String workflowRef) {
        return this.workflowInstanceRepository.findByRefAndSerialNoMax(workflowRef);
    }
}
