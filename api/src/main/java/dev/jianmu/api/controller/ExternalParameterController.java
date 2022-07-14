package dev.jianmu.api.controller;

import dev.jianmu.api.dto.ExternalParameterCreatingDto;
import dev.jianmu.api.dto.ExternalParameterUpdatingDto;
import dev.jianmu.application.service.ExternalParameterApplication;
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

    public ExternalParameterController(ExternalParameterApplication externalParameterApplication) {
        this.externalParameterApplication = externalParameterApplication;
    }

    @PostMapping
    @Operation(summary = "创建外部参数", description = "创建外部参数")
    public void create(@RequestBody @Valid ExternalParameterCreatingDto dto) {
        this.externalParameterApplication.create(dto.getName(), dto.getType(), dto.getRef(), dto.getLabel(), dto.getValue());
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除外部参数", description = "删除外部参数")
    public void delete(@PathVariable("id") String id) {
        this.externalParameterApplication.delete(id);
    }

    @PutMapping
    @Operation(summary = "修改外部参数", description = "修改外部参数")
    public void update(@RequestBody @Valid ExternalParameterUpdatingDto dto) {
        this.externalParameterApplication.update(dto.getId(), dto.getValue(), dto.getName(), dto.getLabel(), dto.getType());
    }
}
