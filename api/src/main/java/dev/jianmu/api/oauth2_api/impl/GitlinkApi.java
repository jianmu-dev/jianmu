package dev.jianmu.api.oauth2_api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.oauth2_api.OAuth2Api;
import dev.jianmu.api.oauth2_api.config.OAuth2Properties;
import dev.jianmu.api.oauth2_api.exception.AccessTokenDoesNotExistException;
import dev.jianmu.api.oauth2_api.exception.GetTokenRequestParameterErrorException;
import dev.jianmu.api.oauth2_api.exception.JsonParseException;
import dev.jianmu.api.oauth2_api.impl.vo.*;
import dev.jianmu.api.oauth2_api.vo.UserInfoVo;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author huangxi
 * @class GitlinkApi
 * @description GitlinkApi
 * @create 2021-06-30 14:08
 */
@Component
public class GitlinkApi implements OAuth2Api {
    private final RestTemplate restTemplate;
    private final OAuth2Properties oAuth2Properties;

    public GitlinkApi(RestTemplate restTemplate, OAuth2Properties oAuth2Properties) {
        this.restTemplate = restTemplate;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public String getAuthUrl(String redirectUri) {
        redirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String responseType = URLEncoder.encode(this.oAuth2Properties.getGitlink().getResponseType(), StandardCharsets.UTF_8);
        String callUrl = "/oauth/authorize?client_id=" + URLEncoder.encode(this.oAuth2Properties.getGitlink().getClientId(), StandardCharsets.UTF_8);
        return this.oAuth2Properties.getGitlink().getBaseUrl() + "oauth2?" +
                "call_url=" + callUrl +
                "&redirect_uri=" + redirectUri +
                "&response_type=" + responseType;
    }

    @Override
    public String getAccessToken(String code, String redirectUri) {
        // 封装请求条件
        GitlinkLoginVo gitlinkLoginVo = GitlinkLoginVo.builder()
                .client_id(this.oAuth2Properties.getGitlink().getClientId())
                .client_secret(this.oAuth2Properties.getGitlink().getClientSecret())
                .code(code)
                .grant_type(this.oAuth2Properties.getGitlink().getGrantType())
                .redirect_uri(redirectUri)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String gitlinkLoginJson;
        try {
            gitlinkLoginJson = mapper.writeValueAsString(gitlinkLoginVo);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(gitlinkLoginJson, headers);

        // 发送请求, 获取token
        ResponseEntity<String> tokenEntity;
        try {
            tokenEntity = this.restTemplate.exchange(this.oAuth2Properties.getGitlink().getTokenUrl(), HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new GetTokenRequestParameterErrorException();
        }

        GitlinkTokenVo gitlinkTokenVo;
        try {
            gitlinkTokenVo = mapper.readValue(tokenEntity.getBody(), GitlinkTokenVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }
        return gitlinkTokenVo.getAccess_token();
    }

    @Override
    public UserInfoVo getUserInfoVo(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer");
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(headers);

        String userTokenInfoUrl = this.oAuth2Properties.getGitlink().getUserInfoUrl() + "?access_token=" + token;

        ResponseEntity<String> userInfoEntity;
        try {
            userInfoEntity = this.restTemplate.exchange(
                    userTokenInfoUrl,
                    HttpMethod.GET,
                    param,
                    String.class);
        } catch (HttpClientErrorException e) {
            throw new AccessTokenDoesNotExistException();
        }

        String userInfo = userInfoEntity.getBody();
        GitlinkUserInfoVo gitlinkUserInfoVo;
        ObjectMapper mapper = new ObjectMapper();
        try {
            gitlinkUserInfoVo = mapper.readValue(userInfo, GitlinkUserInfoVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }

        return UserInfoVo.builder()
                .id(String.valueOf(gitlinkUserInfoVo.getUser_id()))
                .avatarUrl(this.oAuth2Properties.getGitlink().getBaseUrl() + gitlinkUserInfoVo.getImage_url())
                .nickname(gitlinkUserInfoVo.getUsername())
                .username(gitlinkUserInfoVo.getLogin())
                .data(userInfo)
                .build();
    }
}
