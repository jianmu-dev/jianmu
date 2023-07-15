package dev.jianmu.oauth2.api.vo;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author Daihw
 * @class AllowLoginVo
 * @description AllowLoginVo
 * @create 2022/12/6 3:41 下午
 */
@Getter
@Setter
public class AllowLoginVo {
    private List<String> user;
    private List<Organization> organization;

    @Getter
    @Setter
    public static class Organization {
        @NotBlank(message = "oauth2.git.organization.account配置不能为空")
        private String account;
        private String role = "all";
    }
}
