package dev.jianmu.git.repo.aggregate;

/**
 * @author laoji
 * @class Flow
 * @description 流水线
 * @create 2022-07-03 15:41
 */
public class Flow {
    /**
     * 项目id
     */
    private String projectId;

    /**
     * 分支名
     */
    private String branchName;

    public Flow() {
    }

    public Flow(String projectId, String branchName) {
        this.projectId = projectId;
        this.branchName = branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getBranchName() {
        return branchName;
    }
}
