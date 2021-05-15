package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.mybatis.dsl.ProjectRepositoryImpl;
import dev.jianmu.project.aggregate.DslSourceCode;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.repository.DslSourceCodeRepository;
import dev.jianmu.project.repository.GitRepoRepository;
import dev.jianmu.task.repository.InputParameterRepository;
import dev.jianmu.task.repository.ParameterReferRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @class: ProjectApplication
 * @description: ProjectApplication
 * @author: Ethan Liu
 * @create: 2021-05-15 22:13
 **/
@Service
public class ProjectApplication {
    private static final Logger logger = LoggerFactory.getLogger(ProjectApplication.class);

    private final ProjectRepositoryImpl projectRepository;
    private final DslSourceCodeRepository dslSourceCodeRepository;
    private final GitRepoRepository gitRepoRepository;
    private final WorkflowRepository workflowRepository;
    private final InputParameterRepository inputParameterRepository;
    private final ParameterReferRepository parameterReferRepository;
    private final ApplicationEventPublisher publisher;

    public ProjectApplication(
            ProjectRepositoryImpl projectRepository,
            DslSourceCodeRepository dslSourceCodeRepository,
            GitRepoRepository gitRepoRepository,
            WorkflowRepository workflowRepository,
            InputParameterRepository inputParameterRepository,
            ParameterReferRepository parameterReferRepository,
            ApplicationEventPublisher publisher
    ) {
        this.projectRepository = projectRepository;
        this.dslSourceCodeRepository = dslSourceCodeRepository;
        this.gitRepoRepository = gitRepoRepository;
        this.workflowRepository = workflowRepository;
        this.inputParameterRepository = inputParameterRepository;
        this.parameterReferRepository = parameterReferRepository;
        this.publisher = publisher;
    }

    public void trigger(String projectId) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        this.publisher.publishEvent(project);
    }

    @Transactional
    public void deleteById(String id) {
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.workflowRepository.deleteByRef(project.getWorkflowRef());
        this.dslSourceCodeRepository.deleteByProjectId(project.getId());
        this.parameterReferRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.inputParameterRepository.deleteByProjectId(project.getId());
        this.gitRepoRepository.deleteById(project.getGitRepoId());
    }

    public PageInfo<Project> findAll(String workflowName, int pageNum, int pageSize) {
        return this.projectRepository.findAll(workflowName, pageNum, pageSize);
    }

    public Optional<Project> findById(String dslId) {
        return this.projectRepository.findById(dslId);
    }

    public DslSourceCode findByRefAndVersion(String ref, String version) {
        return this.dslSourceCodeRepository.findByRefAndVersion(ref, version).orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
    }
}
