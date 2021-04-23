package dev.jianmu.api.controller;

import dev.jianmu.application.service.TriggerApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @class: TriggerController
 * @description: 触发器接口类
 * @author: Ethan Liu
 * @create: 2021-04-12 10:22
 **/
@RestController
@RequestMapping("trigger")
@Tag(name = "触发器接口", description = "提供触发API")
public class TriggerController {
    private final TriggerApplication triggerApplication;

    @Inject
    public TriggerController(TriggerApplication triggerApplication) {
        this.triggerApplication = triggerApplication;
    }

    @PutMapping("/{triggerId}")
    @Operation(summary = "触发接口", description = "触发启动", hidden = true)
    public void trigger(@Parameter(description = "触发器ID") @PathVariable String triggerId) {
        this.triggerApplication.trigger(triggerId);
    }
}
