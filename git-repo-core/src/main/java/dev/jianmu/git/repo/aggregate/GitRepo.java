package dev.jianmu.git.repo.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author laoji
 * @class GitRepo
 * @description git仓库
 * @create 2022-07-03 15:39
 */
public class GitRepo {

    // TODO 删除仓库时，同步删除所有流水线对应的项目

    /**
     * git仓库id
     */
    private String id;

    /**
     * 所有分支
     */
    private List<Branch> branches = List.of();

    /**
     * 所有流水线
     */
    private List<Flow> flows = new ArrayList<>();

    public GitRepo() {
    }

    public GitRepo(String id) {
        this.id = id;
    }

    /**
     * 同步分支
     *
     * @param branches
     */
    public void syncBranches(List<Branch> branches) {
        this.branches = branches;
        var defaultBranch = this.findDefaultBranch();
        this.flows.forEach(flow -> {
            if (this.branches.stream().anyMatch(branch -> flow.getBranchName().equals(branch.getName()))) {
                return;
            }
            flow.setBranchName(defaultBranch.getName());
        });
    }

    private Branch findDefaultBranch() {
        return this.branches.stream()
                .filter(Branch::getIsDefault)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到默认分支：" + this.id));
    }

    public void addFlow(Flow flow) {
        this.flows.add(flow);
    }

    public void removeFlow(String projectId) {
        this.flows.stream()
                .filter(flow -> flow.getProjectId().equals(projectId))
                .findFirst()
                .ifPresent(this.flows::remove);
    }

    public Optional<Flow> findFlowByProjectId(String projectId) {
        return this.flows.stream()
                .filter(flow -> flow.getProjectId().equals(projectId))
                .findFirst();
    }

    public String getId() {
        return id;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public List<Flow> getFlows() {
        return flows;
    }
}
