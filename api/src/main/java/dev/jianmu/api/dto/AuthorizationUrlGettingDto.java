package dev.jianmu.api.dto;

import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class AuthorizationUrlGettingDto
 * @description 返回授权url的dto
 * @create 2021-06-30 14:08
 */
@Getter
@Setter
public class AuthorizationUrlGettingDto {
    /**
     * 第三方平台类型
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
     * 转换type为枚举类型
     *
     * @return
     */
    public ThirdPartyTypeEnum thirdPartyType() {
        return ThirdPartyTypeEnum.valueOf(this.thirdPartyType);
    }
}
