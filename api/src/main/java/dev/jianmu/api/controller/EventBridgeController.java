package dev.jianmu.api.controller;

import dev.jianmu.api.dto.EbDto;
import dev.jianmu.api.mapper.TargetMapper;
import dev.jianmu.application.service.EventBridgeApplication;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.project.aggregate.Project;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

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
    private final ProjectApplication projectApplication;

    public EventBridgeController(EventBridgeApplication eventBridgeApplication, ProjectApplication projectApplication) {
        this.eventBridgeApplication = eventBridgeApplication;
        this.projectApplication = projectApplication;
    }

    @PutMapping
    @Operation(summary = "保存EventBridge接口", description = "新建或更新EventBridge")
    public EbDto save(@RequestBody @Validated EbDto ebDto) {
        var targets = TargetMapper.INSTANCE.toTargetList(ebDto.getTargets());
        var bridge = this.eventBridgeApplication.saveOrUpdate(ebDto.getBridge(), ebDto.getSource(), targets);
        var source = this.eventBridgeApplication.findSourceByBridgeId(bridge.getId());
        var newTargets = this.eventBridgeApplication.findTargetsByBridgeId(bridge.getId()).stream()
                .map(target -> {
                    var projectName = "";
                    if (target.getDestinationId() != null) {
                        projectName = this.projectApplication.findById(target.getDestinationId())
                                .map(Project::getWorkflowName).orElse("");
                    }
                    return TargetMapper.INSTANCE.toTargetDto(target, projectName);
                })
                .collect(Collectors.toList());
        return EbDto.builder()
                .bridge(bridge)
                .source(source)
                .targets(newTargets)
                .build();
    }

    @DeleteMapping("/{bridgeId}")
    @Operation(summary = "删除EventBridge接口", description = "删除EventBridge")
    public void delete(@PathVariable String bridgeId) {
        this.eventBridgeApplication.delete(bridgeId);
    }
}
