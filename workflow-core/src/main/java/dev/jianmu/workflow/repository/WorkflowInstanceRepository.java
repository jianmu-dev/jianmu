package dev.jianmu.workflow.repository;

import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;

import java.util.List;
import java.util.Optional;

public interface WorkflowInstanceRepository {

    List<WorkflowInstance> findByRefAndVersionAndStatuses(String workflowRef, String workflowVersion, List<ProcessStatus> statuses);

    List<WorkflowInstance> findByWorkflowRefLimit(String workflowRef, long offset);

    Optional<WorkflowInstance> findById(String instanceId);

    Optional<WorkflowInstance> findByTriggerId(String triggerId);

    void add(WorkflowInstance workflowInstance);

    void save(WorkflowInstance workflowInstance);

    void commitEvents(WorkflowInstance workflowInstance);

    List<WorkflowInstance> findAll(int pageNum, int pageSize);

    Optional<WorkflowInstance> findByRefAndSerialNoMax(String workflowRef);

    List<WorkflowInstance> findByRefOffset(String workflowRef, long offset);

    void deleteByWorkflowRef(String workflowRef);

    void deleteById(String id);
}
