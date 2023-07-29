package dev.jianmu.api.dto;

import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class Oauth2LoggingDto
 * @description 登录dto
 * @create 2021-06-30 14:08
 */
@Getter
@Setter
public class Oauth2LoggingDto {
    /**
     * code值
     */
    @NotBlank(message = "请输入授权码")
    private String code;

    /**
     * 第三方登录平台
     */
    @NotNull
    private String thirdPartyType;

    /**
     * 回调地址
     */
    @NotBlank(message = "回调地址不能为空")
    @Pattern(regexp = "^$|(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]", message = "请输入正确的地址")
    private String redirectUri;

    /**
     * 仓库名
     */
    private String gitRepo;

    /**
     * 仓库所有者
     */
    private String gitRepoOwner;

    /**
     * 转换type为枚举类型
     *
     * @return
     */
    public ThirdPartyTypeEnum thirdPartyType() {
        return ThirdPartyTypeEnum.valueOf(this.thirdPartyType);
    }
}
