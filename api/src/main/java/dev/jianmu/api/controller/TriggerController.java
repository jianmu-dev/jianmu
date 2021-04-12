package dev.jianmu.api.controller;

import dev.jianmu.application.service.TriggerApplication;
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
public class TriggerController {
    private final TriggerApplication triggerApplication;

    @Inject
    public TriggerController(TriggerApplication triggerApplication) {
        this.triggerApplication = triggerApplication;
    }

    @PutMapping("/{triggerId}")
    public void trigger(@PathVariable String triggerId) {
        this.triggerApplication.trigger(triggerId);
    }
}
