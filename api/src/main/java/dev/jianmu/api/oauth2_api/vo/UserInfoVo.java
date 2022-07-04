package dev.jianmu.api.oauth2_api.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * @author huangxi
 * @class UserInfoVo
 * @description UserInfoVo
 * @create 2021-06-30 14:08
 */
@Getter
@Builder
public class UserInfoVo {
    /**
     * 第三方平台用户id
     */
    private String id;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 数据
     */
    private String data;

    /**
     * 用户名
     */
    private String username;
}
