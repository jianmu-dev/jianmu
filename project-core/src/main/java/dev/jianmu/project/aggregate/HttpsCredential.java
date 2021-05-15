package dev.jianmu.project.aggregate;

/**
 * @class: HttpsCredential
 * @description: HttpsCredential
 * @author: Ethan Liu
 * @create: 2021-05-15 11:43
 **/
public class HttpsCredential extends Credential {
    private String userKey;
    private String passKey;

    public String getUserKey() {
        return userKey;
    }

    public String getPassKey() {
        return passKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }
}
