package dev.jianmu.trigger.aggregate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @class: WebhookAuth
 * @description: WebhookAuth
 * @author: Ethan Liu
 * @create: 2021-11-10 11:25
 */
public class WebhookAuth {
    private String token;
    private String value;

    public String getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    public static final class Builder {
        private String token;
        private String value;

        private Builder() {
        }

        public static Builder aWebhookAuth() {
            return new Builder();
        }

        private String isSecret(String paramValue) {
            Pattern pattern = Pattern.compile("^\\(\\(([a-zA-Z0-9_-]+\\.*[a-zA-Z0-9_-]+)\\)\\)$");
            Matcher matcher = pattern.matcher(paramValue);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

        public Builder token(String token) {
            var secret = this.isSecret(token);
            if (secret == null) {
                throw new IllegalArgumentException("Token必须使用密钥表达式类型：" + token);
            }
            this.token = token;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public WebhookAuth build() {
            WebhookAuth webhookAuth = new WebhookAuth();
            webhookAuth.token = this.token;
            webhookAuth.value = this.value;
            return webhookAuth;
        }
    }

    @Override
    public String toString() {
        return "WebhookAuth{" +
                "token='" + token + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
