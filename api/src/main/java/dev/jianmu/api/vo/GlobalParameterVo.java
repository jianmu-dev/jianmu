package dev.jianmu.api.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * @class GlobalParameterVo
 * @description GlobalParameterVo
 * @author Daihw
 * @create 2022/7/19 2:53 下午
 */
public class GlobalParameterVo {
    private String ref;
    private String name;
    private String type;
    private Object value;
    private Boolean required;
    private Boolean hidden;
}
