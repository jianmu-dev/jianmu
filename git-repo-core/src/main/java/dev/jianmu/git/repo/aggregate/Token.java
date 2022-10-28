package dev.jianmu.git.repo.aggregate;


import java.util.Date;

/**
 * @author huangxi
 * @class AuthModel
 * @description AuthModel
 * @create 2022/10/9/3:31 下午
 */
public class Token {
    /**
     * 令牌
     */
    private String accessToken;
    /**
     * 令牌类型
     */
    private String tokenType;
    /**
     * 过期时间
     */
    private Integer expiresIn;
    /**
     * 范围
     */
    private String scope;
    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 是否需要创建TokenModel
     *
     * @return
     */
    public boolean exist() {
        if (this.accessToken != null && this.accessToken.length() != 0) {
            return true;
        }

        return new Date().getTime() < (this.createdAt + this.expiresIn) * 1000L;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public Long getCreateAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
