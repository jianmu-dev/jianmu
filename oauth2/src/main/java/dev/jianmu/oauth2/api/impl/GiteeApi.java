package dev.jianmu.oauth2.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.exception.*;
import dev.jianmu.oauth2.api.impl.dto.gitee.LoggingDto;
import dev.jianmu.oauth2.api.impl.vo.gitee.*;
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
 * @class GiteeApi
 * @description GiteeApi
 * @create 2021-06-30 14:08
 */
@Component
public class GiteeApi implements OAuth2Api {
    private final static ObjectMapper MAPPER = new ObjectMapper();
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
    public ITokenVo getAccessToken(String code, String redirectUri) {
        // 封装请求条件
        LoggingDto giteeLoginVo = LoggingDto.builder()
                .clientId(this.oAuth2Properties.getGitee().getClientId())
                .clientSecret(this.oAuth2Properties.getGitee().getClientSecret())
                .code(code)
                .grantType(this.oAuth2Properties.getGitee().getGrantType())
                .redirectUri(redirectUri)
                .build();


        String giteeLoginJson;
        try {
            giteeLoginJson = MAPPER.writeValueAsString(giteeLoginVo);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(giteeLoginJson, headers);

        // 发送请求, 获取token
        ResponseEntity<String> tokenEntity;
        try {
            tokenEntity = this.restTemplate.exchange(this.oAuth2Properties.getGitee().getTokenUrl(), HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new GetTokenRequestParameterErrorException(e.getMessage());
        } catch (ServerErrorException e) {
            throw new HttpServerException(e.getMessage());
        }

        TokenVo giteeTokenVo;
        try {
            giteeTokenVo = MAPPER.readValue(tokenEntity.getBody(), TokenVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return giteeTokenVo;
    }

    @Override
    public IUserInfoVo getUserInfo(String accessToken) {
        String userTokenInfoUrl = this.oAuth2Properties.getGitee().getApiUrl() + "user?access_token=" + accessToken;
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
        UserInfoVo giteeUserInfoVo;
        try {
            giteeUserInfoVo = MAPPER.readValue(userInfo, UserInfoVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        return giteeUserInfoVo;
    }

    @Override
    public IRepoVo getRepo(String accessToken, String gitRepo, String gitRepoOwner) {
        String repoUrl = this.oAuth2Properties.getGitee().getApiUrl() + "repos/" +
                gitRepoOwner + "/" + gitRepo + "?access_token=" + accessToken;
        ResponseEntity<String> entity;
        try {
            entity = this.restTemplate.exchange(
                    repoUrl,
                    HttpMethod.GET,
                    null,
                    String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED) ||
                    e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new NoPermissionException(e.getMessage());
            }
            throw new HttpClientException(e.getMessage());
        } catch (HttpServerException e) {
            throw new HttpServerException(e.getMessage());
        }

        RepoVo repoVo;
        try {
            repoVo = MAPPER.readValue(entity.getBody(), RepoVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        return repoVo;
    }

    @Override
    public List<? extends IRepoMemberVo> getRepoMembers(String accessToken, String gitRepo, String gitRepoOwner) {
        String repoMemberUrl = this.oAuth2Properties.getGitee().getApiUrl() + "repos/" +
                gitRepoOwner + "/" + gitRepo + "/" + "collaborators" + "?access_token=" + accessToken
                + "&page=1&per_page=1000";
        ResponseEntity<String> entity;
        try {
            entity = this.restTemplate.exchange(
                    repoMemberUrl,
                    HttpMethod.GET,
                    null,
                    String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED) ||
                    e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new NoPermissionException(e.getMessage());
            }
            throw new HttpClientException(e.getMessage());
        } catch (HttpServerException e) {
            throw new HttpServerException(e.getMessage());
        }
        List<RepoMembersVo> repoMembersVos;
        try {
            repoMembersVos = MAPPER.readValue(entity.getBody(), new TypeReference<>() {
            });
            repoMembersVos.forEach(m -> m.setOwner(gitRepoOwner));
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        return repoMembersVos;
    }

    @Override
    public IBranchesVo getBranches(String accessToken, String gitRepo, String gitRepoOwner) {
        String branchesUrl = this.oAuth2Properties.getGitee().getApiUrl() + "repos/" +
                gitRepoOwner + "/" + gitRepo + "/branches" + "?access_token=" + accessToken;
        ResponseEntity<String> entity;
        try {
            entity = this.restTemplate.exchange(
                    branchesUrl,
                    HttpMethod.GET,
                    null,
                    String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED) ||
                    e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new NoPermissionException(e.getMessage());
            }
            throw new HttpClientException(e.getMessage());
        } catch (HttpServerException e) {
            throw new HttpServerException(e.getMessage());
        }

        List<BranchesVo.Branch> branches;
        try {
            branches = MAPPER.readValue(entity.getBody(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return BranchesVo.builder()
                .branches(branches)
                .build();
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
    public void createFile(String accessToken, String owner, String repo, String content, String filepath, String author_email, String author_name, String committer_email, String committer_name, String branch, String message) {

    }

    @Override
    public void deleteFile(String accessToken, String owner, String repo, String content, String filepath, String author_email, String author_name, String committer_email, String committer_name, String branch, String message) {

    }

    @Override
    public void updateFile(String accessToken, String owner, String repo, String content, String filepath, String author_email, String author_name, String committer_email, String committer_name, String branch, String message) {

    }


    @Override
    public IFileVo getFile(String accessToken, String gitRepoOwner, String gitRepo, String filepath, String ref) {
        return null;
    }

}
