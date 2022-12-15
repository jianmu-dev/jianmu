package dev.jianmu.api.controller;

import dev.jianmu.api.dto.KVPairDto;
import dev.jianmu.api.dto.NamespaceDto;
import dev.jianmu.api.mapper.KVPairDtoMapper;
import dev.jianmu.api.mapper.NamespaceDtoMapper;
import dev.jianmu.application.service.SecretApplication;
import dev.jianmu.jianmu_user_context.holder.UserSessionHolder;
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
    private final UserSessionHolder userSessionHolder;

    public SecretController(SecretApplication secretApplication, UserSessionHolder userSessionHolder) {
        this.secretApplication = secretApplication;
        this.userSessionHolder = userSessionHolder;
    }

    @PostMapping("/namespaces")
    @Operation(summary = "创建命名空间", description = "创建命名空间")
    public void createNamespace(@RequestBody @Valid NamespaceDto namespaceDto) {
        var namespace = NamespaceDtoMapper.INSTANCE.toNamespace(namespaceDto);
        var session = this.userSessionHolder.getSession();
        namespace.updateAssociation(session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform());
        this.secretApplication.createNamespace(namespace);
    }

    @DeleteMapping("/namespaces/{name}")
    @Operation(summary = "删除命名空间", description = "删除命名空间")
    public void deleteNamespace(@PathVariable String name) {
        var session = this.userSessionHolder.getSession();
        this.secretApplication.deleteNamespace(session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform(), name);
    }

    @PostMapping("/namespaces/{name}")
    @Operation(summary = "新增键值对", description = "新增键值对")
    public void createKVPair(@PathVariable String name, @RequestBody @Valid KVPairDto kvPairDto) {
        var session = this.userSessionHolder.getSession();
        var kv = KVPairDtoMapper.INSTANCE.toKVPair(kvPairDto);
        kv.setNamespaceName(name);
        kv.updateAssociation(session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform());
        this.secretApplication.createKVPair(kv);
    }

    @DeleteMapping("/namespaces/{name}/{key}")
    @Operation(summary = "删除键值对", description = "删除键值对")
    public void deleteKVPair(@PathVariable String name, @PathVariable String key) {
        var session = this.userSessionHolder.getSession();
        this.secretApplication.deleteKVPair(session.getAssociationId(), session.getAssociationType(), session.getAssociationPlatform(), name, key);
    }
}
