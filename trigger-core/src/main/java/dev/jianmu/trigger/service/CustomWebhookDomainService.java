package dev.jianmu.trigger.service;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomWebhookDomainService {

    private List<String> getGitLinkEvents(List<CustomWebhookInstance.EventInstance> eventInstances) {
        if (eventInstances == null) {
            return List.of("push");
        }
        return eventInstances.stream()
                .map(eventInstance -> {
                    switch (eventInstance.getRef()) {
                        case "pr":
                            return List.of("pull_request_only", "pull_request_assign");
                        case "create_tag":
                            return List.of("create");
                        // 默认为push事件
                        default:
                            return List.of("push");
                    }
                })
                .collect(Collectors.flatMapping(Collection::stream, Collectors.toList()));
    }

    public List<String> getGitEvents(String gitType, List<CustomWebhookInstance.EventInstance> eventInstances) {
        switch (gitType) {
            case "GITEE":
                return List.of();
            case "GITLINK":
                return this.getGitLinkEvents(eventInstances);
            case "GITLAB":
                return List.of();
            default:
                throw new RuntimeException("未集成Git：" + gitType);
        }
    }
}
