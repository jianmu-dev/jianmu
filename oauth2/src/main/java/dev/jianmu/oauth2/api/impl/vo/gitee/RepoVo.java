package dev.jianmu.oauth2.api.impl.vo.gitee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.oauth2.api.vo.IRepoVo;
import lombok.*;

/**
 * @author huangxi
 * @class GiteeRepoVo
 * @description GiteeRepoVo
 * @create 2022-07-06 15:15
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoVo implements IRepoVo {
    @JsonProperty("id")
    private long _id;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("default_branch")
    private String _defaultBranch;

    @Override
    @JsonIgnore
    public String getId() {
        return String.valueOf(this._id);
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
}
