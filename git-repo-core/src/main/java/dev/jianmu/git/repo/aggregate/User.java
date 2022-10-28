package dev.jianmu.git.repo.aggregate;

/**
 * @class User
 * @description User
 * @author Daihw
 * @create 2022/10/27 10:27 上午
 */
public class User {
    /**
     * 第三方平台用户id
     */
    private String userId;
    private String login;
    private String imageUrl;
    private String username;

    public String getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUsername() {
        return username;
    }
}
