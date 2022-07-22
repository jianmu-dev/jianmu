package dev.jianmu.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class NodeOutputDefinitionVo
 * @description NodeOutputDefinitionVo
 * @create 2022/7/22 2:11 下午
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeOutputDefinitionVo {
    private String ref;
    private String name;
    private String type;
}
