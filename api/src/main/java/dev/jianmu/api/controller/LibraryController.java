package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.NodeDefinitionDto;
import dev.jianmu.api.dto.PageDto;
import dev.jianmu.api.vo.NodeDefVo;
import dev.jianmu.api.vo.PageUtils;
import dev.jianmu.application.service.HubApplication;
import dev.jianmu.hub.intergration.aggregate.NodeDefinitionVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/nodes")
    @Operation(summary = "节点定义列表", description = "节点定义列表查询", deprecated = true)
    public PageInfo<NodeDefVo> findNodeAll(PageDto dto) {
        var page = this.hubApplication.findPage(
                dto.getPageNum(),
                dto.getPageSize()
        );
        var nodes = page.getList();
        List<NodeDefVo> nodeDefVos = nodes.stream().map(nodeDefinition -> {
            var versions = this.hubApplication.findByOwnerRefAndRef(nodeDefinition.getOwnerRef(), nodeDefinition.getRef()).stream()
                    .map(NodeDefinitionVersion::getVersion).collect(Collectors.toList());
            return NodeDefVo.builder()
                    .icon(nodeDefinition.getIcon())
                    .name(nodeDefinition.getName())
                    .ownerName(nodeDefinition.getOwnerName())
                    .ownerType(nodeDefinition.getOwnerType())
                    .ownerRef(nodeDefinition.getOwnerRef())
                    .creatorName(nodeDefinition.getCreatorName())
                    .creatorRef(nodeDefinition.getCreatorRef())
                    .type(nodeDefinition.getType())
                    .description(nodeDefinition.getDescription())
                    .ref(nodeDefinition.getRef())
                    .sourceLink(nodeDefinition.getSourceLink())
                    .documentLink(nodeDefinition.getDocumentLink())
                    .versions(versions)
                    .build();
        }).collect(Collectors.toList());
        PageInfo<NodeDefVo> newPage = PageUtils.pageInfo2PageInfoVo(page);
        newPage.setList(nodeDefVos);
        return newPage;
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
