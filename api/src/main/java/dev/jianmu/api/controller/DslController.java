package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.PageDto;
import dev.jianmu.application.service.DslApplication;
import dev.jianmu.dsl.aggregate.DslReference;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{dslId}")
    @Operation(summary = "同步DSL定义", description = "同步DSL定义")
    public Workflow sync(@PathVariable String dslId) {
        return this.dslApplication.syncDsl(dslId);
    }

    @PostMapping("/{dslUrl}")
    @Operation(summary = "导入DSL定义", description = "导入DSL定义")
    public Workflow importDsl(@PathVariable String dslUrl) {
        var tempDslUrl = "test-dsl.yaml";
        return this.dslApplication.importDsl(tempDslUrl);
    }

    @DeleteMapping("/{dslId}")
    @Operation(summary = "删除DSL定义", description = "删除DSL定义")
    public void deleteById(@PathVariable String dslId) {
        this.dslApplication.deleteById(dslId);
    }

    @GetMapping
    @Operation(summary = "分页查询DSL列表", description = "分页查询DSL列表")
    public PageInfo<DslReference> findAll(PageDto pageDto) {
        return this.dslApplication.findAll(pageDto.getPageNum(), pageDto.getPageSize());
    }
}
