package dev.jianmu.application.dsl.webhook;

import lombok.Getter;

@Getter
public class CustomWebhookEvent {
    /**
     * 唯一标识，用于在dsl中指定
     */
    private String ref;
    /**
     * 名称，用于展示
     */
    private String name;
}
