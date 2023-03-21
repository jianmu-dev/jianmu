package dev.jianmu.api.controller;

import dev.jianmu.application.service.CacheApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/{cacheId}")
    @Operation(summary = "清理缓存", description = "清理缓存")
    public void clean(@PathVariable String cacheId) {
        this.cacheApplication.clean(cacheId);
    }
}
