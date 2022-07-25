package dev.jianmu.application.service.vo;

import dev.jianmu.application.service.vo.impl.GitRepoData;
import dev.jianmu.application.util.AssociationUtil;

/**
 * @author huangxi
 * @class AssociationData
 * @description AssociationData
 * @create 2022-07-22 17:07
 */
public abstract class AssociationData {
    public static GitRepoData buildGitRepo(String ref, String owner){
        return GitRepoData.builder()
                .ref(ref)
                .owner(owner)
                .build();
    }
}
