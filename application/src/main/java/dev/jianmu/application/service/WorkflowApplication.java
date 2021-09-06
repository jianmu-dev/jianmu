package dev.jianmu.application.service;

import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @program: workflow
 * @description: workflow门面类
 * @author: Ethan Liu
 * @create: 2021-01-22 13:35
 **/
@Service
public class WorkflowApplication {

    private final WorkflowRepository workflowRepository;

    @Inject
    public WorkflowApplication(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    // 创建流程定义
    @Transactional
    public Workflow create(Workflow workflow) {

        Workflow newWorkflow = this.workflowRepository.findByRefAndVersion(workflow.getRef(), workflow.getVersion())
                .orElseGet(() ->
                        Workflow.Builder
                                .aWorkflow()
                                .name(workflow.getName())
                                .ref(workflow.getRef())
                                .description(workflow.getDescription())
                                .nodes(workflow.getNodes())
                                .build()
                );
        return this.workflowRepository.add(newWorkflow);
    }

    // 删除流程定义版本
    @Transactional
    public void deleteByRefAndVersion(String ref, String version) {
        this.workflowRepository.deleteByRefAndVersion(ref, version);
    }

    public List<Workflow> findByRef(String ref) {
        return this.workflowRepository.findByRef(ref);
    }

    public Optional<Workflow> findByRefAndVersion(String ref, String version) {
        return this.workflowRepository.findByRefAndVersion(ref, version);
    }
}
