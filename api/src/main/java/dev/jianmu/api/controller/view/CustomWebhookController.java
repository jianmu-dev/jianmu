package dev.jianmu.api.controller.view;

import dev.jianmu.api.mapper.CustomWebhookDefinitionMapper;
import dev.jianmu.api.vo.*;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.CustomWebhookDefinitionApplication;
import dev.jianmu.application.util.CustomWebhookRuleUtil;
import dev.jianmu.jianmu_user_context.holder.UserSessionHolder;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("view/trigger/webhook/custom")
@Tag(name = "查询自定义Webhook API", description = "查询自定义Webhook API")
public class CustomWebhookController {
    private final CustomWebhookDefinitionApplication definitionApplication;
    private final UserSessionHolder userSessionHolder;

    public CustomWebhookController(CustomWebhookDefinitionApplication definitionApplication, UserSessionHolder userSessionHolder) {
        this.definitionApplication = definitionApplication;
        this.userSessionHolder = userSessionHolder;
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
        var session = this.userSessionHolder.getSession();
        var ref = Arrays.stream(ThirdPartyTypeEnum.values())
                .filter(t -> t.name().equals(session.getAssociationPlatform()))
                .findFirst()
                .map(t -> t.name().toUpperCase())
                .orElse(null);
        return this.definitionApplication.findByRef(ref).stream()
                .map(CustomWebhookDefinitionMapper.INSTANCE::toWebhookDefinitionVo)
                .collect(Collectors.toList());
    }

    @GetMapping("/{ownerRef}/{ref}/versions")
    @Operation(summary = "查询webhook version列表", description = "查询webhook version列表")
    public WebhookDefinitionVersionListVo findVersionByOwnerRefAndRef(@PathVariable("ownerRef") String ownerRef, @PathVariable("ref") String ref) {
        return WebhookDefinitionVersionListVo.builder()
                .versions(this.definitionApplication.findVersionByOwnerRefAndRef(ownerRef, ref).stream()
                        .map(CustomWebhookDefinitionVersion::getVersion)
                        .collect(Collectors.toList()))
                .build();
    }

    @GetMapping("/{ownerRef}/{ref}/{version}/versions")
    @Operation(summary = "获取webhook version", description = "获取webhook version")
    public WebhookDefinitionVersionVo findVersionByOwnerRefAndRefAndVersion(@PathVariable("ownerRef") String ownerRef, @PathVariable("ref") String ref, @PathVariable String version) {
        var definitionVersion = this.definitionApplication.findVersionByOwnerRefAndRefAndVersion(ownerRef, ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到version"));
        return CustomWebhookDefinitionMapper.INSTANCE.toWebhookDefinitionVersionVo(definitionVersion);
    }

    @PostMapping
    public void test(HttpServletRequest request) throws Exception {
        var reader = request.getReader();
        var dslText = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            dslText.append(line).append(System.lineSeparator());
        }
        this.definitionApplication.updateVersion(dslText.toString());
    }
}
