package dev.jianmu.workflow.repository;

import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;

import java.util.List;
import java.util.Optional;

public interface WorkflowInstanceRepository {

    List<WorkflowInstance> findByRefAndVersionAndStatus(String workflowRef, String workflowVersion, ProcessStatus status);

    Optional<WorkflowInstance> findById(String instanceId);

    Optional<WorkflowInstance> findByTriggerId(String triggerId);

    WorkflowInstance add(WorkflowInstance workflowInstance);

    WorkflowInstance save(WorkflowInstance workflowInstance);

    List<WorkflowInstance> findAll(int pageNum, int pageSize);

    Optional<WorkflowInstance> findByRefAndSerialNoMax(String workflowRef);

    void deleteByWorkflowRef(String workflowRef);
}
