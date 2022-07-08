package dev.jianmu.api.oauth2_api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.oauth2_api.OAuth2Api;
import dev.jianmu.api.oauth2_api.config.OAuth2Properties;
import dev.jianmu.api.oauth2_api.exception.AccessTokenDoesNotExistException;
import dev.jianmu.api.oauth2_api.exception.GetTokenRequestParameterErrorException;
import dev.jianmu.api.oauth2_api.exception.JsonParseException;
import dev.jianmu.api.oauth2_api.impl.dto.gitee.LoggingDto;
import dev.jianmu.api.oauth2_api.impl.vo.gitee.TokenVo;
import dev.jianmu.api.oauth2_api.impl.vo.gitee.UserInfoVo;
import dev.jianmu.api.oauth2_api.vo.IBranchesVo;
import dev.jianmu.api.oauth2_api.vo.IRepoMemberVo;
import dev.jianmu.api.oauth2_api.vo.IRepoVo;
import dev.jianmu.api.oauth2_api.vo.IUserInfoVo;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author huangxi
 * @class GiteeApi
 * @description GiteeApi
 * @create 2021-06-30 14:08
 */
@Component
public class GiteeApi implements OAuth2Api {
    private final RestTemplate restTemplate;
    private final OAuth2Properties oAuth2Properties;

    public GiteeApi(RestTemplate restTemplate, OAuth2Properties oAuth2Properties) {
        this.restTemplate = restTemplate;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public String getAuthUrl(String redirectUri) {
        redirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String clientId = URLEncoder.encode(this.oAuth2Properties.getGitee().getClientId(), StandardCharsets.UTF_8);
        String responseType = URLEncoder.encode(this.oAuth2Properties.getGitee().getResponseType(), StandardCharsets.UTF_8);
        String scope = URLEncoder.encode(this.oAuth2Properties.getGitee().getScope(), StandardCharsets.UTF_8);
        return this.oAuth2Properties.getGitee().getCodeUrl() + "?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=" + responseType +
                "&scope=" + scope;
    }

    @Override
    public String getAccessToken(String code, String redirectUri) {
        // 封装请求条件
        LoggingDto giteeLoginVo = LoggingDto.builder()
                .client_id(this.oAuth2Properties.getGitee().getClientId())
                .client_secret(this.oAuth2Properties.getGitee().getClientSecret())
                .code(code)
                .grant_type(this.oAuth2Properties.getGitee().getGrantType())
                .redirect_uri(redirectUri)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String giteeLoginJson;
        try {
            giteeLoginJson = mapper.writeValueAsString(giteeLoginVo);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(giteeLoginJson, headers);

        // 发送请求, 获取token
        ResponseEntity<String> tokenEntity;
        try {
            tokenEntity = this.restTemplate.exchange(this.oAuth2Properties.getGitee().getTokenUrl(), HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new GetTokenRequestParameterErrorException();
        }

        TokenVo giteeTokenVo;
        try {
            giteeTokenVo = mapper.readValue(tokenEntity.getBody(), TokenVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }
        return giteeTokenVo.getAccess_token();
    }

    @Override
    public IUserInfoVo getUserInfo(String token) {
        String userTokenInfoUrl = this.oAuth2Properties.getGitee().getUserInfoUrl() + "?access_token=" + token;
        ResponseEntity<String> userInfoEntity;
        try {
            userInfoEntity = this.restTemplate.exchange(
                    userTokenInfoUrl,
                    HttpMethod.GET,
                    null,
                    String.class);
        } catch (HttpClientErrorException e) {
            throw new AccessTokenDoesNotExistException();
        }

        String userInfo = userInfoEntity.getBody();
        UserInfoVo giteeUserInfoVo;
        ObjectMapper mapper = new ObjectMapper();
        try {
            giteeUserInfoVo = mapper.readValue(userInfo, UserInfoVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }

        return giteeUserInfoVo;
    }

    @Override
    public IRepoVo getRepo(String accessToken, String gitRepo, String gitRepoOwner) {
        return null;
    }

    @Override
    public List<? extends IRepoMemberVo> getRepoMembers(String accessToken, String gitRepo, String gitRepoOwner) {
        return null;
    }

    @Override
    public IBranchesVo getBranches(String accessToken, String gitRepo, String gitRepoOwner) {
        return null;
    }
}
