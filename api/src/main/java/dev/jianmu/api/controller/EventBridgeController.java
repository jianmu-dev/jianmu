package dev.jianmu.api.controller;

import dev.jianmu.api.dto.EbDto;
import dev.jianmu.api.dto.TransformerDto;
import dev.jianmu.api.mapper.TargetMapper;
import dev.jianmu.application.service.EventBridgeApplication;
import dev.jianmu.eventbridge.aggregate.Transformer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @class: EventBridgeController
 * @description: EventBridgeController
 * @author: Ethan Liu
 * @create: 2021-09-26 15:39
 **/
@RestController
@RequestMapping("eb")
@Tag(name = "EventBridge", description = "EventBridge API")
@SecurityRequirement(name = "bearerAuth")
public class EventBridgeController {
    private final EventBridgeApplication eventBridgeApplication;

    public EventBridgeController(EventBridgeApplication eventBridgeApplication) {
        this.eventBridgeApplication = eventBridgeApplication;
    }

    @PutMapping
    @Operation(summary = "保存EventBridge接口", description = "新建或更新EventBridge")
    public void save(@RequestBody @Validated EbDto ebDto) {
        var targets = TargetMapper.INSTANCE.toTargetList(ebDto.getTargets());
        this.eventBridgeApplication.saveOrUpdate(ebDto.getBridge(), ebDto.getSource(), targets);
    }

    @DeleteMapping("/{bridgeId}")
    @Operation(summary = "删除EventBridge接口", description = "删除EventBridge")
    public void delete(@PathVariable String bridgeId) {
        this.eventBridgeApplication.delete(bridgeId);
    }
}
