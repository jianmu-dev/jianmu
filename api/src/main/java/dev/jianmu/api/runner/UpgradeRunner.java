package dev.jianmu.api.runner;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.repository.ProjectLinkGroupRepository;
import dev.jianmu.workflow.aggregate.definition.Condition;
import dev.jianmu.workflow.repository.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class UpgradeRunner
 * @description UpgradeRunner
 * @create 2022-03-14 09:54
 */
@Component
@Slf4j
public class UpgradeRunner implements ApplicationRunner {
    private final WorkflowRepository workflowRepository;
    private final ProjectApplication projectApplication;
    private final ProjectLinkGroupRepository projectLinkGroupRepository;

    public UpgradeRunner(WorkflowRepository workflowRepository, ProjectApplication projectApplication, ProjectLinkGroupRepository projectLinkGroupRepository) {
        this.workflowRepository = workflowRepository;
        this.projectApplication = projectApplication;
        this.projectLinkGroupRepository = projectLinkGroupRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 查找需要升级的项目
        var projects = this.projectApplication.findAll().stream()
                .filter(project -> project.getDslType() == Project.DslType.WORKFLOW)
                .filter(project -> {
                    var workflow = this.workflowRepository.findByRefAndVersion(project.getWorkflowRef(), project.getWorkflowVersion())
                            .orElseThrow(() -> new DataNotFoundException("未找到项目对应的流程定义"));
                    var conditions = workflow.getNodes().stream()
                            .filter(node -> node instanceof Condition)
                            .collect(Collectors.toList());
                    var count = conditions.stream()
                            .map(node -> (Condition) node)
                            .filter(condition -> condition.getBranches() == null)
                            .count();
                    return count > 0;
                })
                .collect(Collectors.toList());
        // 更新项目DSL，升级Workflow
        projects.forEach(project -> {
            var group = this.projectLinkGroupRepository.findByProjectId(project.getId())
                    .orElseThrow(() -> new DataNotFoundException("未找到归属的项目组"));
            var text = project.getDslText() + "\n";
            this.projectApplication.updateProject(project.getId(), text, group.getProjectGroupId());
            log.info("项目- {} -升级成功", project.getWorkflowName());
        });
    }
}
