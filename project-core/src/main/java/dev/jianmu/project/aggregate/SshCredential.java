package dev.jianmu.project.aggregate;

/**
 * @class: SshCredential
 * @description: SshCredential
 * @author: Ethan Liu
 * @create: 2021-05-15 11:41
 **/
public class SshCredential extends Credential {
    private String secretKey;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
