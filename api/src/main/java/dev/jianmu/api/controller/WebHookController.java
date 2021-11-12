package dev.jianmu.api.controller;

import dev.jianmu.application.service.TriggerApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @class: WebHookController
 * @description: WebHookController
 * @author: Ethan Liu
 * @create: 2021-06-25 23:04
 **/
@Controller
@RequestMapping("webhook")
@Tag(name = "WebHook API", description = "WebHook API")
public class WebHookController {
    private final TriggerApplication triggerApplication;

    public WebHookController(TriggerApplication triggerApplication) {
        this.triggerApplication = triggerApplication;
    }


    @RequestMapping(value = "/{projectName}", method = RequestMethod.POST, consumes = {"application/json", "application/x-www-form-urlencoded", "text/plain"})
    @ResponseBody
    @Operation(summary = "触发项目", description = "触发项目启动")
    public void receivePostJsonEvent(
            HttpServletRequest request,
            @RequestHeader("Content-Type") String contentType,
            @PathVariable String projectName
    ) {
        this.triggerApplication.receiveHttpEvent(projectName, request, contentType);
    }
}
