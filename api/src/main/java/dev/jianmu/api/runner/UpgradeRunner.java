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
                .filter(project -> project.getTriggerType() == Project.TriggerType.WEBHOOK)
                .collect(Collectors.toList());
        // 更新项目DSL，升级Workflow
        projects.forEach(project -> {
            var group = this.projectLinkGroupRepository.findByProjectId(project.getId())
                    .orElseThrow(() -> new DataNotFoundException("未找到归属的项目组"));
            var text = project.getDslText() + "\n";
            try {
                this.projectApplication.updateProject(project.getId(), text, group.getProjectGroupId(), null, null, null, null, null, false);
            } catch (Exception e) {
                log.warn("项目- {}升级失败：", project.getWorkflowName(), e);
            }
            log.info("项目- {} -升级成功", project.getWorkflowName());
        });
    }
}
