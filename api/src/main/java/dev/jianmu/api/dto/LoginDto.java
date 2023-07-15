package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * @class LoginDto
 * @description LoginDto
 * @author Ethan Liu
 * @create 2021-05-18 09:45
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "LoginDto")
public class LoginDto {
    @Schema(required = true)
    @NotBlank(message = "username不能为空")
    private String username;

    @Schema(required = true)
    @NotBlank(message = "password不能为空")
    private String password;
}
