package dev.jianmu.api.controller;

import dev.jianmu.api.dto.DslTextDto;
import dev.jianmu.api.dto.ProjectCreatingDto;
import dev.jianmu.api.vo.ProjectIdVo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.api.vo.TriggerProjectVo;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
import dev.jianmu.jianmu_user_context.holder.UserSessionHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Ethan Liu
 * @class ProjectController
 * @description ProjectController
 * @create 2021-05-14 14:00
 */
@RestController
@RequestMapping("projects")
@Tag(name = "项目API", description = "项目API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    private final ProjectApplication projectApplication;
    private final GitRepoApplication gitRepoApplication;
    private final UserSessionHolder userSessionHolder;
    private final WorkflowInstanceInternalApplication workflowInstanceInternalApplication;

    public ProjectController(
            ProjectApplication projectApplication,
            GitRepoApplication gitRepoApplication,
            UserSessionHolder userSessionHolder,
            WorkflowInstanceInternalApplication workflowInstanceInternalApplication
    ) {
        this.projectApplication = projectApplication;
        this.gitRepoApplication = gitRepoApplication;
        this.userSessionHolder = userSessionHolder;
        this.workflowInstanceInternalApplication = workflowInstanceInternalApplication;
    }

    @PutMapping("/enable/{projectId}")
    @Operation(summary = "激活项目", description = "激活项目")
    public void enable(@PathVariable String projectId) {
        this.projectApplication.switchEnabled(this.userSessionHolder.getAccountId(), projectId, true);
    }

    @PutMapping("/disable/{projectId}")
    @Operation(summary = "禁用项目", description = "禁用项目")
    public void disable(@PathVariable String projectId) {
        this.projectApplication.switchEnabled(this.userSessionHolder.getAccountId(), projectId, false);
    }

    @PostMapping("/trigger/{projectId}")
    @Operation(summary = "触发项目", description = "触发项目启动")
    public TriggerProjectVo trigger(@Parameter(description = "触发器ID") @PathVariable String projectId) {
        var session = this.userSessionHolder.getSession();
        var triggerEvent = this.projectApplication.triggerByManual(projectId, session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform());
        return TriggerProjectVo.builder()
                .triggerId(triggerEvent.getTriggerId())
                .build();
    }

    @PostMapping
    @Operation(summary = "创建项目", description = "上传DSL并创建项目")
    public ProjectIdVo createProject(@RequestBody @Valid DslTextDto dslTextDto) {
        var session = this.userSessionHolder.getSession();
        if (session.getAssociationId() != null) {
            if (dslTextDto.getBranch() == null) {
                throw new RuntimeException("请选择正确的分支");
            }
            if (this.gitRepoApplication.findBranches(session.getAssociationId()).stream()
                    .noneMatch(branch -> branch.getName().equals(dslTextDto.getBranch()))) {
                throw new RuntimeException("请选择正确的分支");
            }
        }
        var project = this.projectApplication.createProject(session.getAccountId(), session.getAccountId(), dslTextDto.getDslText(), dslTextDto.getProjectGroupId(), session.getAssociationPlatformUserId(), session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform(), dslTextDto.getBranch(), true);
        return ProjectIdVo.builder().id(project.getId()).build();
    }

    @PostMapping("inner")
    @Operation(summary = "内部创建项目", description = "内部创建项目")
    public ProjectIdVo createProject(@Valid @RequestBody ProjectCreatingDto projectCreatingDto) {
        var accountId = this.userSessionHolder.getAccountId();
        var project = this.projectApplication.createProject(projectCreatingDto.getCreatorId(), accountId, projectCreatingDto.getDslText(), projectCreatingDto.getProjectGroupId(), "", projectCreatingDto.getAssociationId(), projectCreatingDto.getAssociationType(), projectCreatingDto.getAssociationPlatform(), null, false);
        return ProjectIdVo.builder().id(project.getId()).build();
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目", description = "根据ID更新项目DSL定义")
    public void updateProject(@PathVariable String projectId, @RequestBody @Valid DslTextDto dslTextDto) {
        var session = this.userSessionHolder.getSession();
        var concurrent = this.projectApplication.updateProject(session.getAccountId(), projectId, dslTextDto.getDslText(), dslTextDto.getProjectGroupId(), session.getAssociationPlatformUserId(), session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform(), true);
        // 并发执行正在排队的流程实例
        if (concurrent) {
            var project = this.projectApplication.findById(projectId, session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform())
                    .orElseThrow(() -> new DataNotFoundException("未找到的项目"));
            this.workflowInstanceInternalApplication.start(project.getWorkflowRef(), null);
        }
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目", description = "删除项目")
    public void deleteById(@PathVariable String projectId) {
        var session = this.userSessionHolder.getSession();
        this.projectApplication.deleteById(projectId, session.getAssociationPlatformUserId(), session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform());
    }
}
