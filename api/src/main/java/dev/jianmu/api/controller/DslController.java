package dev.jianmu.api.controller;

import dev.jianmu.application.service.DslApplication;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class: DslController
 * @description: DSL API
 * @author: Ethan Liu
 * @create: 2021-04-20 20:03
 **/
@RestController
@RequestMapping("dsl")
@Tag(name = "DSL", description = "DSL API")
public class DslController {
    private final DslApplication dslApplication;

    public DslController(DslApplication dslApplication) {
        this.dslApplication = dslApplication;
    }

    @GetMapping
    public Workflow show() {
        return this.dslApplication.importDsl();
    }
}
