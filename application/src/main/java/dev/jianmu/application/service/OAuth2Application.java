package dev.jianmu.application.service;

import dev.jianmu.application.exception.OAuth2EntryException;
import dev.jianmu.application.exception.OAuth2IsNotAuthorizedException;
import dev.jianmu.application.service.vo.Association;
import dev.jianmu.application.service.vo.AssociationData;
import dev.jianmu.application.service.vo.impl.GitRepoData;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.git.repo.aggregate.Branch;
import dev.jianmu.git.repo.aggregate.GitRepo;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.RoleEnum;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.NoPermissionException;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.oauth2.api.vo.IRepoMemberVo;
import dev.jianmu.oauth2.api.vo.IRepoVo;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangxi
 * @class OAuth2Application
 * @description OAuth2Application
 * @create 2022-07-22 16:32
 */
@Service
public class OAuth2Application {
    private final GitRepoRepository gitRepoRepository;
    private final AssociationUtil associationUtil;
    private final OAuth2Properties oAuth2Properties;

    public OAuth2Application(GitRepoRepository gitRepoRepository, AssociationUtil associationUtil, OAuth2Properties oAuth2Properties) {
        this.gitRepoRepository = gitRepoRepository;
        this.associationUtil = associationUtil;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Transactional
    public Association getAssociation(ThirdPartyTypeEnum thirdPartyType, String accessToken, IUserInfoVo userInfo, AssociationData associationData, String userId) {
        String associationType = this.associationUtil.getAssociationType();
        if (this.associationUtil.getAssociationType() == null) {
            return Association.builder()
                    .role(null)
                    .id(null)
                    .data(associationData)
                    .type(associationType)
                    .build();
        }
        OAuth2Api oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(thirdPartyType)
                .userId(userId)
                .build();

        RoleEnum role = null;
        String associationId = null;
        if (associationData instanceof GitRepoData) {
            var data = (GitRepoData) associationData;
            IRepoVo repo = this.checkEntry(accessToken, data.getRef(), data.getOwner(), oAuth2Api);
            role = this.mappingPermissions(oAuth2Api, userInfo, accessToken, data.getRef(), data.getOwner());
            associationId = repo.getId();

            // 同步仓库
            var branches = oAuth2Api.getBranches(accessToken, repo.getRepo(), repo.getOwner()).getBranchNames();
            var isCreated = this.syncBranches(repo.getId(), repo.getRepo(), repo.getOwner(), repo.getDefaultBranch(), branches);
            if (isCreated) {
                var webhookUrl = this.oAuth2Properties.getWebhookHost() + "projects/sync";
                oAuth2Api.createWebhook(accessToken, repo.getOwner(), repo.getRepo(), webhookUrl, true, null);
            }
        } else {
            //TODO 待扩展其他
        }

        return Association.builder()
                .role(role)
                .id(associationId)
                .data(associationData)
                .type(associationType)
                .build();
    }

    private Boolean syncBranches(String id, String ref, String owner, String defaultBranch, List<String> branchesString) {
        var branches = branchesString.stream()
                .map(name -> new Branch(name, name.equals(defaultBranch)))
                .collect(Collectors.toList());
        var gitRepo = this.gitRepoRepository.findById(id)
                .orElse(new GitRepo(id));
        boolean isCreated = gitRepo.getOwner() == null;
        // 同步分支
        gitRepo.syncBranches(ref, owner, branches);
        this.gitRepoRepository.saveOrUpdate(gitRepo);
        return isCreated;
    }

    /**
     * 准入配置是否正确
     *
     * @param accessToken
     * @param gitRepo
     * @param gitRepoOwner
     * @param oAuth2Api
     * @return
     */
    private IRepoVo checkEntry(String accessToken, String gitRepo, String gitRepoOwner, OAuth2Api oAuth2Api) {
        if (StringUtils.hasLength(gitRepo) && StringUtils.hasLength(gitRepoOwner)) {
            IRepoVo repo;
            try {
                repo = oAuth2Api.getRepo(accessToken, gitRepo, gitRepoOwner);
                if (repo != null) {
                    return repo;
                }
            } catch (NoPermissionException e) {
                throw new OAuth2IsNotAuthorizedException(e.getMessage());
            }
        }
        throw new OAuth2EntryException("缺少仓库名或仓库所有者信息");
    }

    /**
     * 将其他平台的权限映射成内部的权限
     *
     * @param oAuth2Api
     * @param userInfoVo
     * @return
     */
    private RoleEnum mappingPermissions(OAuth2Api oAuth2Api, IUserInfoVo userInfoVo, String accessToken, String gitRepo, String gitRepoOwner) {
        List<? extends IRepoMemberVo> repoMembers;
        try {
            repoMembers = oAuth2Api.getRepoMembers(accessToken, gitRepo, gitRepoOwner);
        } catch (NoPermissionException e) {
            throw new OAuth2IsNotAuthorizedException(e.getMessage());
        }
        for (IRepoMemberVo member : repoMembers) {
            if (member.getUsername().equals(userInfoVo.getUsername())) {
                if (member.isOwner()) {
                    return RoleEnum.OWNER;
                } else if (member.isAdmin()) {
                    return RoleEnum.ADMIN;
                } else {
                    return RoleEnum.MEMBER;
                }
            }
        }
        throw new OAuth2IsNotAuthorizedException("没有权限操作此仓库");
    }
}
