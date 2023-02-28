package dev.jianmu.api.controller;

import dev.jianmu.application.service.CacheApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * @author <a href="https://gitee.com/ethan-liu">Ethan Liu</a>
 * @date 2023-02-24 19:30
 */
@RestController
@RequestMapping("caches")
@Tag(name = "缓存API", description = "缓存API")
@SecurityRequirement(name = "bearerAuth")
public class CacheController {
    private final CacheApplication cacheApplication;

    public CacheController(CacheApplication cacheApplication) {
        this.cacheApplication = cacheApplication;
    }

    @PostMapping("/{projectId}/{cacheName}")
    @Operation(summary = "创建缓存", description = "创建缓存")
    public void create(@PathVariable String projectId, @PathVariable String cacheName) {
        this.cacheApplication.create(projectId, cacheName);
    }

    @DeleteMapping("/{cacheId}")
    @Operation(summary = "删除缓存", description = "删除缓存")
    public void delete(@PathVariable String cacheId) {
        this.cacheApplication.deleteById(cacheId);
    }
}
