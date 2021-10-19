package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @class: PayloadVo
 * @description: PayloadVo
 * @author: Ethan Liu
 * @create: 2021-10-14 18:11
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "PayloadVo")
public class PayloadVo {
    private Map<String, List<String>> header;
    private Map<String, String[]> query;
    private String body;
}
