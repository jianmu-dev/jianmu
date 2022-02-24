package dev.jianmu.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class WebRequestPayloadVo
 * @description WebRequestPayloadVo
 * @create 2022/2/24 3:45 下午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRequestPayloadVo {
    private String payload;
}
