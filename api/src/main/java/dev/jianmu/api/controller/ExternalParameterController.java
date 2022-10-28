package dev.jianmu.api.controller;

import dev.jianmu.api.dto.ExternalParameterCreatingDto;
import dev.jianmu.api.dto.ExternalParameterUpdatingDto;
import dev.jianmu.api.util.UserContextHolder;
import dev.jianmu.application.service.ExternalParameterApplication;
import dev.jianmu.application.util.AssociationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author huangxi
 * @class ParameterController
 * @description ParameterController
 * @create 2022-07-13 13:47
 */
@RestController
@RequestMapping("external_parameters")
@Tag(name = "参数控制器", description = "参数控制器")
public class ExternalParameterController {
    private final ExternalParameterApplication externalParameterApplication;
    private final UserContextHolder userContextHolder;
    private final AssociationUtil associationUtil;

    public ExternalParameterController(ExternalParameterApplication externalParameterApplication, UserContextHolder userContextHolder, AssociationUtil associationUtil) {
        this.externalParameterApplication = externalParameterApplication;
        this.userContextHolder = userContextHolder;
        this.associationUtil = associationUtil;
    }

    @PostMapping
    @Operation(summary = "创建外部参数", description = "创建外部参数")
    public void create(@RequestBody @Valid ExternalParameterCreatingDto dto) {
        var session = this.userContextHolder.getSession();
        this.externalParameterApplication.create(dto.getName(), dto.getType(), dto.getRef(), dto.getLabel(), dto.getValue(), session.getAssociationId(), session.getAssociationType());
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除外部参数", description = "删除外部参数")
    public void delete(@PathVariable("id") String id) {
        var session = this.userContextHolder.getSession();
        this.externalParameterApplication.delete(id, session.getAssociationId(), session.getAssociationType());
    }

    @PutMapping
    @Operation(summary = "修改外部参数", description = "修改外部参数")
    public void update(@RequestBody @Valid ExternalParameterUpdatingDto dto) {
        var session = this.userContextHolder.getSession();
        this.externalParameterApplication.update(dto.getId(), dto.getValue(), dto.getName(), dto.getLabel(), dto.getType(), session.getAssociationId(), session.getAssociationType());
    }
}
