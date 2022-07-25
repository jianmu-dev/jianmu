package dev.jianmu.api.controller;

import dev.jianmu.api.dto.KVPairDto;
import dev.jianmu.api.dto.NamespaceDto;
import dev.jianmu.api.jwt.UserContextHolder;
import dev.jianmu.api.mapper.KVPairDtoMapper;
import dev.jianmu.api.mapper.NamespaceDtoMapper;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.application.service.SecretApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Ethan Liu
 * @class SecretController
 * @description 密钥管理接口
 * @create 2021-04-19 19:46
 */
@RestController
@RequestMapping("secrets")
@Tag(name = "密钥管理接口", description = "提供密钥创建删除等API")
@SecurityRequirement(name = "bearerAuth")
public class SecretController {
    private final SecretApplication secretApplication;
    private final UserContextHolder userContextHolder;
    private final AssociationUtil associationUtil;

    public SecretController(SecretApplication secretApplication, UserContextHolder userContextHolder, AssociationUtil associationUtil) {
        this.secretApplication = secretApplication;
        this.userContextHolder = userContextHolder;
        this.associationUtil = associationUtil;
    }

    @PostMapping("/namespaces")
    @Operation(summary = "创建命名空间", description = "创建命名空间")
    public void createNamespace(@RequestBody @Valid NamespaceDto namespaceDto) {
        var namespace = NamespaceDtoMapper.INSTANCE.toNamespace(namespaceDto);
        namespace.setAssociationId(this.userContextHolder.getSession().getGitRepoId());
        namespace.setAssociationType(this.associationUtil.getAssociationType());
        this.secretApplication.createNamespace(namespace);
    }

    @DeleteMapping("/namespaces/{name}")
    @Operation(summary = "删除命名空间", description = "删除命名空间")
    public void deleteNamespace(@PathVariable String name) {
        var repoId = this.userContextHolder.getSession().getGitRepoId();
        var type = this.associationUtil.getAssociationType();
        this.secretApplication.deleteNamespace(repoId, type, name);
    }

    @PostMapping("/namespaces/{name}")
    @Operation(summary = "新增键值对", description = "新增键值对")
    public void createKVPair(@PathVariable String name, @RequestBody @Valid KVPairDto kvPairDto) {
        var kv = KVPairDtoMapper.INSTANCE.toKVPair(kvPairDto);
        kv.setNamespaceName(name);
        kv.setAssociationId(this.userContextHolder.getSession().getGitRepoId());
        kv.setAssociationType(this.associationUtil.getAssociationType());
        this.secretApplication.createKVPair(kv);
    }

    @DeleteMapping("/namespaces/{name}/{key}")
    @Operation(summary = "删除键值对", description = "删除键值对")
    public void deleteKVPair(@PathVariable String name, @PathVariable String key) {
        var repoId = this.userContextHolder.getSession().getGitRepoId();
        var type = this.associationUtil.getAssociationType();
        this.secretApplication.deleteKVPair(repoId, type, name, key);
    }
}
