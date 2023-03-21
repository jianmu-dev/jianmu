package dev.jianmu.api.controller;

import dev.jianmu.api.dto.GitRepoSyncingDto;
import dev.jianmu.api.mapper.ProjectVoMapper;
import dev.jianmu.api.vo.GitRepoBranchVo;
import dev.jianmu.api.vo.GitRepoVo;
import dev.jianmu.api.vo.ProjectVo;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.application.service.internal.WorkflowInternalApplication;
import dev.jianmu.git.repo.aggregate.Flow;
import dev.jianmu.jianmu_user_context.holder.UserSessionHolder;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daihw
 * @class GitRepoController
 * @description GitRepoController
 * @create 2022/7/5 5:22 下午
 */
@RestController
@RequestMapping("git_repos")
@Tag(name = "GitRepo", description = "GitRepo API")
@SecurityRequirement(name = "bearerAuth")
public class GitRepoController {
    private final GitRepoApplication gitRepoApplication;
    private final TriggerApplication triggerApplication;
    private final UserSessionHolder userSessionHolder;
    private final WorkflowInternalApplication workflowInternalApplication;

    public GitRepoController(
            GitRepoApplication gitRepoApplication,
            TriggerApplication triggerApplication,
            UserSessionHolder userSessionHolder,
            WorkflowInternalApplication workflowInternalApplication
    ) {
        this.gitRepoApplication = gitRepoApplication;
        this.triggerApplication = triggerApplication;
        this.userSessionHolder = userSessionHolder;
        this.workflowInternalApplication = workflowInternalApplication;
    }

    @PutMapping("/sync")
    @Operation(summary = "同步仓库", description = "同步仓库")
    public void sync(@RequestBody @Valid GitRepoSyncingDto dto) {
        this.gitRepoApplication.sync(dto.getUserId(), dto.getId(), dto.getOwnerRef(), dto.getRef(), dto.getBranches());
    }

    @GetMapping("{id}")
    @Operation(summary = "查询仓库详情", description = "查询仓库详情")
    public GitRepoVo findBranches(@PathVariable("id") String id) {
        var gitRepo = this.gitRepoApplication.findById(id);
        return GitRepoVo.builder()
                .ref(gitRepo.getRef())
                .owner(gitRepo.getOwner())
                .build();
    }

    @GetMapping("/branches")
    @Operation(summary = "查询分支列表", description = "查询分支列表")
    public List<GitRepoBranchVo> findBranches() {
        return this.gitRepoApplication.findBranches(this.userSessionHolder.getSession().getAssociationId()).stream()
                .map(branch -> GitRepoBranchVo.builder()
                        .branchName(branch.getName())
                        .isDefault(branch.getIsDefault())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/flows")
    @Operation(summary = "查询流水线列表", description = "查询流水线列表")
    public List<ProjectVo> findFlows() {
        var gitRepo = this.gitRepoApplication.findById(this.userSessionHolder.getSession().getAssociationId());
        var flows = this.gitRepoApplication.findFlows(gitRepo);
        var refVersions = flows.stream()
                .map(t -> t.getWorkflowRef() + t.getWorkflowVersion())
                .collect(Collectors.toList());
        var workflows = this.workflowInternalApplication.findByRefVersions(refVersions);
        return flows.stream()
                .map(project -> {
                    var projectVo = ProjectVoMapper.INSTANCE.toProjectVo(project);
                    projectVo.setNextTime(this.triggerApplication.getNextFireTime(project.getId()));
                    projectVo.setBranch(gitRepo.getFlows().stream()
                            .filter(flow -> flow.getProjectId().equals(projectVo.getId()))
                            .findFirst()
                            .map(Flow::getBranchName)
                            .orElse(null));
                    projectVo.setCaches(workflows.stream()
                            .filter(workflow -> workflow.getRef().equals(projectVo.getWorkflowRef()))
                            .findFirst()
                            .map(Workflow::getCaches)
                            .orElse(null)
                    );
                    if (project.getStatus() == null) {
                        return projectVo;
                    }
                    if (project.getStatus().equals(ProcessStatus.TERMINATED.name())) {
                        projectVo.setStatus("FAILED");
                    }
                    if (project.getStatus().equals(ProcessStatus.FINISHED.name())) {
                        projectVo.setStatus("SUCCEEDED");
                    }
                    if (project.getStatus().equals(ProcessStatus.SUSPENDED.name())) {
                        projectVo.setSuspendedTime(project.getSuspendedTime());
                        projectVo.setStatus("SUSPENDED");
                    }
                    if (project.getStatus().equals(ProcessStatus.RUNNING.name())) {
                        projectVo.setStartTime(project.getStartTime());
                        projectVo.setStatus("RUNNING");
                    }
                    return projectVo;
                }).collect(Collectors.toList());
    }
}
