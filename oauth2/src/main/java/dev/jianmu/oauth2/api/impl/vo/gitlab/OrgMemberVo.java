package dev.jianmu.oauth2.api.impl.vo.gitlab;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @class OrgMemberVo
 * @description OrgMemberVo
 * @author Daihw
 * @create 2022/12/6 4:48 下午
 */
@Getter
public class OrgMemberVo {
   @JsonProperty(value = "access_level")
   private Integer accessLevel;
}
