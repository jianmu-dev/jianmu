package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.mybatis.workflow.WorkflowInstanceRepositoryImpl;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
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
    private final WorkflowInstanceRepositoryImpl workflowInstanceRepository;
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
        var runnings = this.workflowInstanceRepository.findByWorkflowAndRunningStatusOffset(workflowRef, this.globalProperties.getGlobal().getRecord().getMax());
        var runnings = this.workflowInstanceRepository.findByWorkflowAndRunningStatusLimit(workflowRef, this.globalProperties.getGlobal().getRecord().getMax());
        list.addAll(runnings);
        return list;
    }

    public PageInfo<WorkflowInstance> findPageByWorkflowRef(Integer pageNum, Integer pageSize, String workflowRef) {
        return this.workflowInstanceRepository.findPageByWorkflowRef(pageNum, pageSize, workflowRef);
    }

    public Optional<WorkflowInstance> findByRefAndSerialNoMax(String workflowRef) {
        return this.workflowInstanceRepository.findByRefAndSerialNoMax(workflowRef);
    }

    public Optional<WorkflowInstance> findByTriggerId(String triggerId) {
        return this.workflowInstanceRepository.findByTriggerId(triggerId);
    }
}
