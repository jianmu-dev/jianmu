package dev.jianmu.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class NodeCacheVo
 * @description NodeCacheVo
 * @create 2023/3/8 10:45 上午
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeCacheVo {
    private String name;
    private Boolean available;
    private String path;
}
