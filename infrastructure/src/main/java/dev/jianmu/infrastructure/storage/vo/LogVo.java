package dev.jianmu.infrastructure.storage.vo;

import lombok.*;

/**
 * @class LogVo
 * @description LogVo
 * @author Daihw
 * @create 2022/6/1 4:37 下午
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogVo {
    private String lastEventId;
    private String data;
}
