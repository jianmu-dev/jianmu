package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.KVPairDto;
import dev.jianmu.api.dto.NamespaceDto;
import dev.jianmu.api.dto.NamespaceSearchDto;
import dev.jianmu.api.mapper.KVPairDtoMapper;
import dev.jianmu.api.mapper.NamespaceDtoMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.SecretApplication;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @class: SecretController
 * @description: 密钥管理接口
 * @author: Ethan Liu
 * @create: 2021-04-19 19:46
 **/
@RestController
@RequestMapping("secrets")
@Tag(name = "密钥管理接口", description = "提供密钥创建删除等API")
public class SecretController {
    private final SecretApplication secretApplication;

    public SecretController(SecretApplication secretApplication) {
        this.secretApplication = secretApplication;
    }

    @PostMapping("/namespaces")
    @Operation(summary = "创建命名空间", description = "创建命名空间")
    public void createNamespace(@RequestBody @Valid NamespaceDto namespaceDto) {
        var namespace = NamespaceDtoMapper.INSTANCE.toNamespace(namespaceDto);
        this.secretApplication.createNamespace(namespace);
    }

    @DeleteMapping("/namespaces/{name}")
    @Operation(summary = "删除命名空间", description = "删除命名空间")
    public void deleteNamespace(@PathVariable String name) {
        this.secretApplication.deleteNamespace(name);
    }

    @PostMapping("/namespaces/{name}")
    @Operation(summary = "新增键值对", description = "新增键值对")
    public void createKVPair(@PathVariable String name, @RequestBody @Valid KVPairDto kvPairDto) {
        var kv = KVPairDtoMapper.INSTANCE.toKVPair(kvPairDto);
        kv.setNamespaceName(name);
        this.secretApplication.createKVPair(kv);
    }

    @DeleteMapping("/namespaces/{name}/{key}")
    @Operation(summary = "删除键值对", description = "删除键值对")
    public void deleteKVPair(@PathVariable String name, @PathVariable String key) {
        this.secretApplication.deleteKVPair(name, key);
    }

    @GetMapping("/namespaces/{name}/keys")
    @Operation(summary = "查询键值对列表", description = "查询键值对列表")
    public List<String> findAll(@PathVariable String name) {
        var kvs = this.secretApplication.findAll(name);
        return kvs.stream().map(KVPair::getKey).collect(Collectors.toList());
    }

    @GetMapping("/namespaces/{name}")
    @Operation(summary = "查询命名空间详情", description = "查询命名空间详情")
    public Namespace findByName(@PathVariable String name) {
        return this.secretApplication.findById(name).orElseThrow(() -> new DataNotFoundException("未找到该命名空间"));
    }

    @GetMapping("/namespaces")
    @Operation(summary = "分页查询命名空间列表", description = "分页查询命名空间列表")
    public PageInfo<Namespace> findAll(NamespaceSearchDto namespaceSearchDto) {
        return this.secretApplication.findAll(namespaceSearchDto.getName(), namespaceSearchDto.getPageNum(), namespaceSearchDto.getPageSize());
    }
}