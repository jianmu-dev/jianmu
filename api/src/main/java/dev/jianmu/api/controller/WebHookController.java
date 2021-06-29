package dev.jianmu.api.controller;

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

    public WebHookController(ProjectApplication projectApplication) {
        this.projectApplication = projectApplication;
    }

    @GetMapping("/{webhook}")
    @Operation(summary = "触发项目", description = "触发项目启动")
    public void trigger(@PathVariable String webhook) {
        var bytes = Base64.getDecoder().decode(webhook);
        var strings = new String(bytes, StandardCharsets.UTF_8).split("_");
        this.projectApplication.triggerByWebHook(strings[0], strings[1]);
    }
}
