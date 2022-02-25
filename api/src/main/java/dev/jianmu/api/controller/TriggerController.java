package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.WebRequestDto;
import dev.jianmu.api.vo.WebRequestPayloadVo;
import dev.jianmu.api.vo.WebhookParamVo;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.trigger.aggregate.WebRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Ethan Liu
 * @class TriggerController
 * @description TriggerController
 * @create 2021-11-15 13:36
 */
@RestController
@RequestMapping("trigger")
@Tag(name = "Trigger", description = "Trigger API")
@SecurityRequirement(name = "bearerAuth")
public class TriggerController {
    private final TriggerApplication triggerApplication;
    private final StorageService storageService;

    public TriggerController(TriggerApplication triggerApplication, StorageService storageService) {
        this.triggerApplication = triggerApplication;
        this.storageService = storageService;
    }

    @GetMapping("/web_requests")
    @Operation(summary = "分页返回Webhook请求列表", description = "分页返回Webhook请求列表")
    public PageInfo<WebRequest> listWebRequest(@Validated WebRequestDto dto) {
        return this.triggerApplication.findWebRequestPage(dto.getProjectId(), dto.getPageNum(), dto.getPageSize());
    }

    @GetMapping("/web_requests/{webRequestId}/payload")
    @Operation(summary = "获取payload", description = "获取webhook请求的payload")
    public WebRequestPayloadVo getPayload(@PathVariable String webRequestId) {
        return this.triggerApplication.findWebRequestById(webRequestId)
                .map(webRequest -> WebRequestPayloadVo.builder()
                        .payload(ObjectUtils.isEmpty(webRequest.getPayload()) ? this.storageService.readWebhook(webRequest.getId()) : webRequest.getPayload())
                        .build())
                .orElse(WebRequestPayloadVo.builder()
                        .payload(null)
                        .build());
    }

    @PostMapping("/retry/{webRequestId}")
    @Operation(summary = "Webhook请求重试", description = "Webhook请求重试")
    public void retry(@PathVariable String webRequestId) {
        this.triggerApplication.retryHttpEvent(webRequestId);
    }

    @GetMapping("/web_requests/{id}/trigger")
    @Operation(summary = "获取webhook参数", description = "获取webhook参数")
    public WebhookParamVo getWebhookParam(@PathVariable String id) {
        var webhook = this.triggerApplication.getWebhookParam(id);
        return WebhookParamVo.builder()
                .param(webhook.getParam())
                .auth(webhook.getAuth())
                .only(webhook.getOnly())
                .build();
    }
}
