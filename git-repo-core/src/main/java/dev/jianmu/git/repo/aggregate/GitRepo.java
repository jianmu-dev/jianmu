package dev.jianmu.git.repo.aggregate;

import java.util.List;

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
    private List<Branch> branches;

    /**
     * 所有流水线
     */
    private List<Flow> flows;

    /**
     * 同步分支
     *
     * @param branches
     */
    public void syncBranches(List<Branch> branches) {
        // TODO 同步git平台仓库的分支
        // TODO 同步流水线中的分支数据，若流水线的分支不存在时，把该流水线挪到默认分支中
    }
}
