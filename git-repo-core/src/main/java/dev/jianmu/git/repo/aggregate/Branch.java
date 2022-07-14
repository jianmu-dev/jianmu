package dev.jianmu.git.repo.aggregate;

/**
 * @author laoji
 * @class Branch
 * @description git仓库分支
 * @create 2022-07-03 15:47
 */
public class Branch {
    /**
     * 分支名
     */
    private String name;

    /**
     * 是否默认分支
     */
    private Boolean isDefault;

    public Branch() {
    }

    public Branch(String name, Boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }
}
