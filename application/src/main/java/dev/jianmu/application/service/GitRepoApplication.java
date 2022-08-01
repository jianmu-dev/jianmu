package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.git.repo.aggregate.Branch;
import dev.jianmu.git.repo.aggregate.Flow;
import dev.jianmu.git.repo.aggregate.GitRepo;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.project.query.ProjectVo;
import dev.jianmu.project.repository.ProjectRepository;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Daihw
 * @class GitRepoApplication
 * @description GitRepoApplication
 * @create 2022/7/5 9:45 上午
 */
@Service
public class GitRepoApplication {
    private final GitRepoRepository gitRepoRepository;
    private final ProjectRepository projectRepository;

    public GitRepoApplication(
            GitRepoRepository gitRepoRepository,
            ProjectRepository projectRepository
    ) {
        this.gitRepoRepository = gitRepoRepository;
        this.projectRepository = projectRepository;
    }

    public GitRepo findById(String id) {
        return this.gitRepoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + id));
    }

    @Transactional
    public void addFlow(String projectId, String branch, String gitRepoId) {
        if (gitRepoId == null) {
            return;
        }
        var gitRepo = this.gitRepoRepository.findById(gitRepoId)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + gitRepoId));
        gitRepo.addFlow(new Flow(projectId, branch));
        this.gitRepoRepository.saveOrUpdate(gitRepo);
    }

    @Transactional
    public void removeFlow(String projectId, String gitRepoId) {
        if (gitRepoId == null) {
            return;
        }
        var gitRepo = this.gitRepoRepository.findById(gitRepoId)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + gitRepoId));
        gitRepo.removeFlow(projectId);
        this.gitRepoRepository.saveOrUpdate(gitRepo);
    }

    public List<ProjectVo> findFlows(GitRepo gitRepo) {
        var projectIds = gitRepo.getFlows().stream()
                .map(Flow::getProjectId)
                .collect(Collectors.toList());
        if (projectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.projectRepository.findByIdIn(projectIds);
    }

    public Optional<Flow> findFlowByProjectId(String projectId, String gitRepoId) {
        if (gitRepoId == null) {
            return Optional.empty();
        }
        var gitRepo = this.gitRepoRepository.findById(gitRepoId)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + gitRepoId));
        return gitRepo.findFlowByProjectId(projectId);
    }

    public List<Branch> findBranches(String id) {
        var gitRepo = this.gitRepoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + id));
        return gitRepo.getBranches();
    }

    public Optional<GitRepo> findByRefAndOwner(String ref, String owner) {
        return this.gitRepoRepository.findByRefAndOwner(ref, owner);
    }
}
