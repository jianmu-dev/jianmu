package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class Auth
 * @description Auth
 * @author Daihw
 * @create 2022/6/9 5:16 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "AuthVo")
public class Auth {
    private String address;
    private String username;
    private String password;
}
