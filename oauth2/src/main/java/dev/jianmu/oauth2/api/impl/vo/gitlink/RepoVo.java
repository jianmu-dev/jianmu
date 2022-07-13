package dev.jianmu.oauth2.api.impl.vo.gitlink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.oauth2.api.vo.IRepoVo;
import lombok.*;

/**
 * @author huangxi
 * @class GitlinkRepoVo
 * @description GitlinkRepoVo
 * @create 2022-07-06 15:15
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoVo implements IRepoVo {
    private String message;
    private String identifier;
    private String name;
    @JsonProperty("project_id")
    private int projectId;
    @JsonProperty("repo_id")
    private int repoId;
    @JsonProperty("issues_count")
    private int issuesCount;
    @JsonProperty("pull_requests_count")
    private int pullRequestsCount;
    @JsonProperty("project_identifier")
    private String projectIdentifier;
    @JsonProperty("praises_count")
    private int praisesCount;
    @JsonProperty("forked_count")
    private int forkedCount;
    @JsonProperty("watchers_count")
    private int watchersCount;
    @JsonProperty("versions_count")
    private int versionsCount;
    @JsonProperty("version_releases_count")
    private int versionReleasesCount;
    @JsonProperty("version_releasesed_count")
    private int versionReleasesedCount;
    @JsonProperty("contributor_users_count")
    private int contributorUsersCount;
    private String permission;
    @JsonProperty("mirror_url")
    private String mirrorUrl;
    private boolean mirror;
    private int type;
    @JsonProperty("open_devops")
    private boolean openDevops;
    @JsonProperty("mirror_status")
    private int mirrorStatus;
    @JsonProperty("mirror_num")
    private int mirrorNum;
    @JsonProperty("first_sync")
    private boolean firstSync;
    private boolean watched;
    private boolean praised;
    private int status;
    @JsonProperty("forked_from_project_id")
    private String forkedFromProjectId;
    private String size;
    @JsonProperty("ssh_url")
    private String sshUrl;
    @JsonProperty("clone_url")
    private String cloneUrl;
    @JsonProperty("default_branch")
    private String _defaultBranch;
    private boolean empty;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("private")
    private boolean _private;
    private AuthorVo author;

    @Override
    public String getId() {
        return String.valueOf(this.projectId);
    }

    @Override
    public String getOwner() {
        return this.fullName.split("/")[0];
    }

    @Override
    public String getRepo() {
        return this.fullName.split("/")[1];
    }

    @Override
    public String getDefaultBranch() {
        return this._defaultBranch;
    }

    @Setter
    @Getter
    public static class AuthorVo {
        private int id;
        private String login;
        private String type;
        private String name;
        private String image_url;
    }
}