package dev.jianmu.api.controller.view;

import dev.jianmu.api.mapper.CustomWebhookDefinitionMapper;
import dev.jianmu.api.vo.*;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.CustomWebhookDefinitionApplication;
import dev.jianmu.application.util.CustomWebhookRuleUtil;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.trigger.repository.CustomWebhookDefinitionVersionRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("view/trigger/webhook/custom")
@Tag(name = "查询自定义Webhook API", description = "查询自定义Webhook API")
public class CustomWebhookController {
    private final CustomWebhookDefinitionApplication definitionApplication;
    @Autowired
    private CustomWebhookDefinitionVersionRepository versionRepository;

    public CustomWebhookController(CustomWebhookDefinitionApplication definitionApplication) {
        this.definitionApplication = definitionApplication;
    }

    @GetMapping("operators")
    @Operation(summary = "获取Webhook运算符", description = "获取Webhook运算符")
    public WebhookOperatorVo findWebhookOperators() {
        return WebhookOperatorVo.builder()
                .paramOperators(Arrays.stream(Parameter.Type.values())
                        .filter(type -> type != Parameter.Type.SECRET)
                        .map(type -> WebhookParamOperatorVo.builder()
                                .type(type)
                                .operators(CustomWebhookRuleUtil.getOperators(type).stream()
                                        .map(operator -> WebhookEventOperatorVo.builder()
                                                .ref(operator.name())
                                                .name(operator.name)
                                                .build())
                                        .collect(Collectors.toList())
                                )
                                .build())
                        .collect(Collectors.toList()))
                .rulesetOperators(Arrays.stream(CustomWebhookInstance.RulesetOperator.values())
                        .map(rulesetOperator -> WebhookEventOperatorVo.builder()
                                .ref(rulesetOperator.name())
                                .name(rulesetOperator.name)
                                .build())
                        .collect(Collectors.toList())
                )
                .build();
    }

    @GetMapping
    @Operation(summary = "查询webhook列表", description = "查询webhook列表")
    public List<WebhookDefinitionVo> findAll() {
        return this.definitionApplication.findAll().stream()
                .map(CustomWebhookDefinitionMapper.INSTANCE::toWebhookDefinitionVo)
                .collect(Collectors.toList());
    }

    @GetMapping("/{ownerRef}/{ref}/versions")
    @Operation(summary = "查询webhook version列表", description = "查询webhook version列表")
    public List<WebhookDefinitionVersionVo> findVersionByOwnerRefAndRef(@PathVariable("ownerRef") String ownerRef, @PathVariable("ref") String ref) {
        return this.definitionApplication.findVersionByOwnerRefAndRef(ownerRef, ref).stream()
                .map(CustomWebhookDefinitionMapper.INSTANCE::toWebhookDefinitionVersionVo)
                .collect(Collectors.toList());
    }

    @GetMapping("/{ownerRef}/{ref}/{version}/versions")
    @Operation(summary = "获取webhook version", description = "获取webhook version")
    public WebhookDefinitionVersionVo findVersionByOwnerRefAndRefAndVersion(@PathVariable("ownerRef") String ownerRef, @PathVariable("ref") String ref, @PathVariable String version) {
        var definitionVersion = this.definitionApplication.findVersionByOwnerRefAndRefAndVersion(ownerRef, ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到version"));
        return CustomWebhookDefinitionMapper.INSTANCE.toWebhookDefinitionVersionVo(definitionVersion);
    }

    @PostMapping
    public void test(@RequestBody CustomWebhookDefinitionVersion customWebhookDefinitionVersion) {
        this.versionRepository.saveOrUpdate(customWebhookDefinitionVersion);
    }
}
