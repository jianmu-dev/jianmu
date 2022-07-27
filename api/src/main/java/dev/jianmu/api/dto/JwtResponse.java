package dev.jianmu.api.dto;

import dev.jianmu.application.service.vo.AssociationData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Ethan Liu
 * @class JwtResponse
 * @description JwtResponse
 * @create 2021-05-18 09:48
 */
@Getter
@Builder
@Schema(description = "JwtResponse")
public class JwtResponse {
    private String type;
    private String message;
    private String token;
    private String id;
    private String username;
    private String avatarUrl;
    private AssociationData associationData;
    private String thirdPartyType;
    private String entryUrl;
}
