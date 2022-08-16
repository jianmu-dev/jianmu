package dev.jianmu.oauth2.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.exception.AccessTokenDoesNotExistException;
import dev.jianmu.oauth2.api.exception.GetTokenRequestParameterErrorException;
import dev.jianmu.oauth2.api.exception.HttpServerException;
import dev.jianmu.oauth2.api.exception.JsonParseException;
import dev.jianmu.oauth2.api.impl.dto.gitlab.LoggingDto;
import dev.jianmu.oauth2.api.impl.vo.gitlab.UserInfoVo;
import dev.jianmu.oauth2.api.impl.vo.gitlab.TokenVo;
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
 * @class GitLabApi
 * @description GitLabApi
 * @create 2022-08-10 14:32
 */
@Component
public class GitLabApi implements OAuth2Api {
    private final static ObjectMapper MAPPER = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final OAuth2Properties oAuth2Properties;

    public GitLabApi(RestTemplate restTemplate, OAuth2Properties oAuth2Properties) {
        this.restTemplate = restTemplate;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public String getAuthUrl(String redirectUri) {
        redirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String clientId = URLEncoder.encode(this.oAuth2Properties.getGitlab().getClientId(), StandardCharsets.UTF_8);
        String responseType = URLEncoder.encode(this.oAuth2Properties.getGitlab().getResponseType(), StandardCharsets.UTF_8);
        String scope = URLEncoder.encode(this.oAuth2Properties.getGitlab().getScope(), StandardCharsets.UTF_8);
        return this.oAuth2Properties.getGitlab().getCodeUrl() + "?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=" + responseType +
                "&scope=" + scope;
    }

    @Override
    public ITokenVo getAccessToken(String code, String redirectUri) {
        // 封装请求条件
        LoggingDto gitlabLoggingDto = LoggingDto.builder()
                .client_id(this.oAuth2Properties.getGitlab().getClientId())
                .client_secret(this.oAuth2Properties.getGitlab().getClientSecret())
                .code(code)
                .grant_type(this.oAuth2Properties.getGitlab().getGrantType())
                .redirect_uri(redirectUri)
                .build();

        String gitlabLoginJson;
        try {
            gitlabLoginJson = MAPPER.writeValueAsString(gitlabLoggingDto);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(gitlabLoginJson, headers);

        // 发送请求, 获取token
        ResponseEntity<String> tokenEntity;
        try {
            tokenEntity = this.restTemplate.exchange(this.oAuth2Properties.getGitlab().getTokenUrl(), HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new GetTokenRequestParameterErrorException(e.getMessage());
        } catch (ServerErrorException e) {
            throw new HttpServerException(e.getMessage());
        }

        TokenVo gitlabTokenVo;
        try {
            gitlabTokenVo = MAPPER.readValue(tokenEntity.getBody(), TokenVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return gitlabTokenVo;
    }

    @Override
    public IUserInfoVo getUserInfo(String accessToken) {
        String userTokenInfoUrl = this.oAuth2Properties.getGitlab().getApiUrl() + "user?access_token=" + accessToken;
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
        UserInfoVo gitlabUserInfoVo;
        try {
            gitlabUserInfoVo = MAPPER.readValue(userInfo, UserInfoVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        return gitlabUserInfoVo;
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

    @Override
    public IWebhookVo createWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active, List<String> events) {
        return null;
    }

    @Override
    public void deleteWebhook(String accessToken, String gitRepoOwner, String gitRepo, String id) {

    }

    @Override
    public void updateWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active, String id, List<String> events) {

    }

    @Override
    public IWebhookVo getWebhook(String accessToken, String gitRepoOwner, String gitRepo, String id) {
        return null;
    }

    @Override
    public void createFile(String accessToken, String owner, String repo, String content, String filepath, String authorEmail, String authorName, String committerEmail, String committerName, String branch, String message) {

    }

    @Override
    public void deleteFile(String accessToken, String owner, String repo, String content, String filepath, String authorEmail, String authorName, String committerEmail, String committerName, String branch, String message) {

    }

    @Override
    public void updateFile(String accessToken, String owner, String repo, String content, String filepath, String authorEmail, String authorName, String committerEmail, String committerName, String branch, String message) {

    }

    @Override
    public IFileVo getFile(String accessToken, String owner, String repo, String filepath, String ref) {
        return null;
    }

}

