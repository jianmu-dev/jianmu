package dev.jianmu.api.controller;

import dev.jianmu.api.jwt.UserContextHolder;
import dev.jianmu.api.mapper.ProjectVoMapper;
import dev.jianmu.api.vo.GitRepoBranchVo;
import dev.jianmu.api.vo.ProjectVo;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.git.repo.aggregate.Flow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final UserContextHolder userContextHolder;

    public GitRepoController(GitRepoApplication gitRepoApplication, TriggerApplication triggerApplication, UserContextHolder userContextHolder) {
        this.gitRepoApplication = gitRepoApplication;
        this.triggerApplication = triggerApplication;
        this.userContextHolder = userContextHolder;
    }

    @GetMapping("/branches")
    @Operation(summary = "查询分支列表", description = "查询分支列表")
    public List<GitRepoBranchVo> findBranches() {
        return this.gitRepoApplication.findBranches(this.userContextHolder.getSession().getAssociationId()).stream()
                .map(branch -> GitRepoBranchVo.builder()
                        .branchName(branch.getName())
                        .isDefault(branch.getIsDefault())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/flows")
    @Operation(summary = "查询流水线列表", description = "查询流水线列表")
    public List<ProjectVo> findFlows() {
        var gitRepo = this.gitRepoApplication.findById(this.userContextHolder.getSession().getAssociationId());
        return this.gitRepoApplication.findFlows(gitRepo)
                .stream()
                .map(project -> {
                    var projectVo = ProjectVoMapper.INSTANCE.toProjectVo(project);
                    projectVo.setNextTime(this.triggerApplication.getNextFireTime(project.getId()));
                    projectVo.setBranch(gitRepo.getFlows().stream()
                            .filter(flow -> flow.getProjectId().equals(projectVo.getId()))
                            .findFirst()
                            .map(Flow::getBranchName)
                            .orElse(null));
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
