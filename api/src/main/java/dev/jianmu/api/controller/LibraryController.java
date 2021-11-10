package dev.jianmu.api.controller;

import dev.jianmu.api.dto.NodeDefinitionDto;
import dev.jianmu.application.service.HubApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @class: LibraryController
 * @description: LibraryController
 * @author: Ethan Liu
 * @create: 2021-09-15 14:10
 **/
@RestController
@RequestMapping("library")
@Tag(name = "Hub", description = "Hub API")
@SecurityRequirement(name = "bearerAuth")
public class LibraryController {
    private final HubApplication hubApplication;

    public LibraryController(HubApplication hubApplication) {
        this.hubApplication = hubApplication;
    }

    @PutMapping("/{ownerRef}/{ref}")
    @Operation(summary = "节点定义同步", description = "节点定义同步")
    public void sync(@PathVariable String ownerRef, @PathVariable String ref) {
        this.hubApplication.syncNode(ownerRef, ref);
    }

    @DeleteMapping("/{ownerRef}/{ref}")
    @Operation(summary = "删除节点定义", description = "删除节点定义")
    public void delete(@PathVariable String ownerRef, @PathVariable String ref) {
        this.hubApplication.deleteNode(ownerRef, ref);
    }

    @PostMapping("/nodes")
    @Operation(summary = "新增节点定义", description = "新增节点定义")
    public void create(@RequestBody @Validated NodeDefinitionDto dto) {
        this.hubApplication.addNode(dto.getName(), dto.getDescription(), dto.getDsl());
    }
}
