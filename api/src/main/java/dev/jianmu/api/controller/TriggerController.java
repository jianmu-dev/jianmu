package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.WebRequestDto;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.trigger.aggregate.WebRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class TriggerController
 * @description TriggerController
 * @author Ethan Liu
 * @create 2021-11-15 13:36
 */
@RestController
@RequestMapping("trigger")
@Tag(name = "Trigger", description = "Trigger API")
@SecurityRequirement(name = "bearerAuth")
public class TriggerController {
    private final TriggerApplication triggerApplication;

    public TriggerController(TriggerApplication triggerApplication) {
        this.triggerApplication = triggerApplication;
    }

    @GetMapping("/web_requests")
    @Operation(summary = "分页返回Webhook请求列表", description = "分页返回Webhook请求列表")
    public PageInfo<WebRequest> listWebRequest(@Validated WebRequestDto dto) {
        return this.triggerApplication.findWebRequestPage(dto.getProjectId(), dto.getPageNum(), dto.getPageSize());
    }
}
