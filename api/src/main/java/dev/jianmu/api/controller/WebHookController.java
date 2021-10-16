package dev.jianmu.api.controller;

import dev.jianmu.application.service.EventBridgeApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
    private final EventBridgeApplication eventBridgeApplication;

    public WebHookController(EventBridgeApplication eventBridgeApplication) {
        this.eventBridgeApplication = eventBridgeApplication;
    }

    @RequestMapping(value = "/{webhook}", method = RequestMethod.POST, consumes = {"application/json", "application/x-www-form-urlencoded", "text/plain"})
    @ResponseBody
    @Operation(summary = "触发项目", description = "触发项目启动")
    public void receivePostJsonEvent(
            HttpServletRequest request,
            @RequestHeader("Content-Type") String contentType,
            @PathVariable String webhook
    ) {
        var bytes = Base64.getDecoder().decode(webhook);
        var strings = new String(bytes, StandardCharsets.UTF_8).split("_");
        var token = strings[0];
        var sourceId = strings[1];
        this.eventBridgeApplication.receiveHttpEvent(token, sourceId, request, contentType);
    }
}
