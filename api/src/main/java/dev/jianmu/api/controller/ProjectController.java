package dev.jianmu.api.controller;

import dev.jianmu.api.dto.DslTextDto;
import dev.jianmu.api.jwt.UserContextHolder;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.api.vo.ProjectIdVo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
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
    private final UserContextHolder userContextHolder;
    private final AssociationUtil associationUtil;
    private final WorkflowInstanceInternalApplication workflowInstanceInternalApplication;

    public ProjectController(
            ProjectApplication projectApplication,
            GitRepoApplication gitRepoApplication,
            UserContextHolder userContextHolder,
            AssociationUtil associationUtil,
            WorkflowInstanceInternalApplication workflowInstanceInternalApplication
    ) {
        this.projectApplication = projectApplication;
        this.gitRepoApplication = gitRepoApplication;
        this.userContextHolder = userContextHolder;
        this.associationUtil = associationUtil;
        this.workflowInstanceInternalApplication = workflowInstanceInternalApplication;
    }

    @PutMapping("/enable/{projectId}")
    @Operation(summary = "激活项目", description = "激活项目")
    public void enable(@PathVariable String projectId) {
        this.projectApplication.switchEnabled(projectId, true);
    }

    @PutMapping("/disable/{projectId}")
    @Operation(summary = "禁用项目", description = "禁用项目")
    public void disable(@PathVariable String projectId) {
        this.projectApplication.switchEnabled(projectId, false);
    }

    @PostMapping("/trigger/{projectId}")
    @Operation(summary = "触发项目", description = "触发项目启动")
    public void trigger(@Parameter(description = "触发器ID") @PathVariable String projectId) {
        var repoId = this.userContextHolder.getSession().getAssociationId();
        this.projectApplication.triggerByManual(projectId, repoId, this.associationUtil.getAssociationType());
    }

    @PostMapping
    @Operation(summary = "创建项目", description = "上传DSL并创建项目")
    public ProjectIdVo createProject(@RequestBody @Valid DslTextDto dslTextDto) {
        var session = this.userContextHolder.getSession();
        if (session.getAssociationId() != null) {
            if (dslTextDto.getBranch() == null) {
                throw new RuntimeException("请选择正确的分支");
            }
            if (this.gitRepoApplication.findBranches(session.getAssociationId()).stream()
                    .noneMatch(branch -> branch.getName().equals(dslTextDto.getBranch()))) {
                throw new RuntimeException("请选择正确的分支");
            }
        }
        var associationType = this.associationUtil.getAssociationType();
        var project = this.projectApplication.createProject(dslTextDto.getDslText(), dslTextDto.getProjectGroupId(), session.getUsername(), session.getAssociationId(), associationType, dslTextDto.getBranch());
        return ProjectIdVo.builder().id(project.getId()).build();
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目", description = "根据ID更新项目DSL定义")
    public void updateProject(@PathVariable String projectId, @RequestBody @Valid DslTextDto dslTextDto) {
        var session = this.userContextHolder.getSession();
        var type = this.associationUtil.getAssociationType();
        var concurrent = this.projectApplication.updateProject(projectId, dslTextDto.getDslText(), dslTextDto.getProjectGroupId(), session.getUsername(), session.getAssociationId(), type);
        // 并发执行正在排队的流程实例
        if (concurrent) {
            var project = this.projectApplication.findById(projectId, session.getAssociationId(), type)
                    .orElseThrow(() -> new DataNotFoundException("未找到的项目"));
            this.workflowInstanceInternalApplication.start(project.getWorkflowRef());
        }
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目", description = "删除项目")
    public void deleteById(@PathVariable String projectId) {
        var repoId = this.userContextHolder.getSession().getAssociationId();
        this.projectApplication.deleteById(projectId, repoId, this.associationUtil.getAssociationType());
    }
}
