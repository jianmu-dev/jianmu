package dev.jianmu.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @class ProjectCacheVo
 * @description ProjectCacheVo
 * @author Daihw
 * @create 2023/3/6 5:36 下午
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCacheVo {
    private String id;
    private String name;
    private Boolean available;
    private String workerId;
    private List<ProjectNodeCacheVo> nodeCaches;

    @Getter
    @Builder
    public static class ProjectNodeCacheVo{
        private final String name;
        private final String metadata;
        private final String path;
    }
}
