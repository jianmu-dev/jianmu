package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: JwtResponse
 * @description: JwtResponse
 * @author: Ethan Liu
 * @create: 2021-05-18 09:48
 **/
@Data
@NoArgsConstructor
@Builder
@Schema(description = "JwtResponse")
public class JwtResponse {
    private String token;
    private final String type = "Bearer";
    private Long id;
    private String username;

    public JwtResponse(String token, Long id, String username) {
        this.token = token;
        this.id = id;
        this.username = username;
    }
}
