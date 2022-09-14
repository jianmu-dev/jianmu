package dev.jianmu.oauth2.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.exception.AccessTokenDoesNotExistException;
import dev.jianmu.oauth2.api.exception.GetTokenRequestParameterErrorException;
import dev.jianmu.oauth2.api.exception.HttpServerException;
import dev.jianmu.oauth2.api.exception.JsonParseException;
import dev.jianmu.oauth2.api.impl.dto.gitea.LoggingDto;
import dev.jianmu.oauth2.api.impl.vo.gitea.TokenVo;
import dev.jianmu.oauth2.api.impl.vo.gitea.UserInfoVo;
import dev.jianmu.oauth2.api.vo.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerErrorException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author huangxi
 * @class GiteaApi
 * @description GiteaApi
 * @create 2022-08-24 13:37
 */
@Component
public class GiteaApi implements OAuth2Api {
    private final static ObjectMapper MAPPER = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final OAuth2Properties oAuth2Properties;

    public GiteaApi(RestTemplate restTemplate, OAuth2Properties oAuth2Properties) {
        this.restTemplate = restTemplate;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public String getAuthUrl(String redirectUri) {
        redirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String clientId = URLEncoder.encode(this.oAuth2Properties.getGitea().getClientId(), StandardCharsets.UTF_8);
        String responseType = URLEncoder.encode(this.oAuth2Properties.getGitea().getResponseType(), StandardCharsets.UTF_8);
        return this.oAuth2Properties.getGitea().getCodeUrl() + "?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=" + responseType;
    }

    @Override
    public String getAccessToken(String code, String redirectUri) {
        // 封装请求条件
        LoggingDto giteaLoggingDto = LoggingDto.builder()
                .clientId(this.oAuth2Properties.getGitea().getClientId())
                .clientSecret(this.oAuth2Properties.getGitea().getClientSecret())
                .code(code)
                .grantType(this.oAuth2Properties.getGitea().getGrantType())
                .redirectUri(redirectUri)
                .build();

        String giteaLoginJson;
        try {
            giteaLoginJson = MAPPER.writeValueAsString(giteaLoggingDto);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(giteaLoginJson, headers);

        // 发送请求, 获取token
        ResponseEntity<String> tokenEntity;
        try {
            tokenEntity = this.restTemplate.exchange(this.oAuth2Properties.getGitea().getTokenUrl(), HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new GetTokenRequestParameterErrorException(e.getMessage());
        } catch (ServerErrorException e) {
            throw new HttpServerException(e.getMessage());
        }

        TokenVo giteaTokenVo;
        try {
            giteaTokenVo = MAPPER.readValue(tokenEntity.getBody(), TokenVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return giteaTokenVo.getAccessToken();
    }

    @Override
    public IUserInfoVo getUserInfo(String accessToken) {
        String userTokenInfoUrl = this.oAuth2Properties.getGitea().getApiUrl() + "user?access_token=" + accessToken;
        ResponseEntity<String> userInfoEntity;
        try {
            userInfoEntity = this.restTemplate.exchange(
                    userTokenInfoUrl,
                    HttpMethod.GET,
                    null,
                    String.class);
        } catch (HttpClientErrorException e) {
            throw new AccessTokenDoesNotExistException(e.getMessage());
        } catch (ServerErrorException e) {
            throw new HttpServerException(e.getMessage());
        }

        String userInfo = userInfoEntity.getBody();
        UserInfoVo giteaUserInfoVo;
        try {
            giteaUserInfoVo = MAPPER.readValue(userInfo, UserInfoVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        return giteaUserInfoVo;
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
