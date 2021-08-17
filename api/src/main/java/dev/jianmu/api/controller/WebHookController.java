package dev.jianmu.api.controller;

import dev.jianmu.application.service.EventBridgeSettingApplication;
import dev.jianmu.application.service.ProjectApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @class: WebHookController
 * @description: WebHookController
 * @author: Ethan Liu
 * @create: 2021-06-25 23:04
 **/
@RestController
@RequestMapping("webhook")
@Tag(name = "WebHook API", description = "WebHook API")
public class WebHookController {
    private final ProjectApplication projectApplication;
    private final EventBridgeSettingApplication eventBridgeSettingApplication;

    public WebHookController(ProjectApplication projectApplication, EventBridgeSettingApplication eventBridgeSettingApplication) {
        this.projectApplication = projectApplication;
        this.eventBridgeSettingApplication = eventBridgeSettingApplication;
    }

    @GetMapping("/{webhook}")
    @Operation(summary = "触发项目", description = "触发项目启动")
    public void trigger(@PathVariable String webhook) {
        var bytes = Base64.getDecoder().decode(webhook);
        var strings = new String(bytes, StandardCharsets.UTF_8).split("_");
        this.projectApplication.triggerByWebHook(strings[1], strings[0]);
    }

    @GetMapping("/test/{projectId}")
    public void addWebhook(@PathVariable String projectId) {
        this.eventBridgeSettingApplication.addWebhook(projectId);
    }
}
