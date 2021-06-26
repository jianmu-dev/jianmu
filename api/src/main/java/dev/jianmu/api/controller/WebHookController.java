package dev.jianmu.api.controller;

import dev.jianmu.application.service.ProjectApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{projectId}/{webhook}")
    @Operation(summary = "触发项目", description = "触发项目启动")
    public void trigger(@PathVariable String projectId, @PathVariable String webhook) {
        this.projectApplication.triggerByWebHook(projectId, webhook);
    }
}
