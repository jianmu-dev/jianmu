package dev.jianmu.api.vo;

import dev.jianmu.secret.aggregate.Namespace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @class NameSpacesVo
 * @description NameSpacesVo
 * @author Ethan Liu
 * @create 2021-11-02 13:18
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "命名空间列表VO")
public class NameSpacesVo {
    private String credentialManagerType;
    private List<Namespace> list;
}
