package dev.jianmu.oauth2.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.impl.dto.gitee.LoggingDto;
import dev.jianmu.oauth2.api.impl.vo.gitee.*;
import dev.jianmu.oauth2.api.vo.IBranchesVo;
import dev.jianmu.oauth2.api.vo.IRepoMemberVo;
import dev.jianmu.oauth2.api.vo.IRepoVo;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
import dev.jianmu.oauth2.api.exception.*;
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
    private final RestTemplate restTemplate;
    private final OAuth2Properties oAuth2Properties;
    private final static ObjectMapper MAPPER = new ObjectMapper();

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


        String giteeLoginJson;
        try {
            giteeLoginJson = MAPPER.writeValueAsString(giteeLoginVo);
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
        } catch (ServerErrorException e) {
            throw new HttpServerException();
        }

        TokenVo giteeTokenVo;
        try {
            giteeTokenVo = MAPPER.readValue(tokenEntity.getBody(), TokenVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }
        return giteeTokenVo.getAccess_token();
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
            throw new AccessTokenDoesNotExistException();
        } catch (ServerErrorException e) {
            throw new HttpServerException();
        }

        String userInfo = userInfoEntity.getBody();
        UserInfoVo giteeUserInfoVo;
        try {
            giteeUserInfoVo = MAPPER.readValue(userInfo, UserInfoVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
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
            throw new JsonParseException();
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
            repoMembersVos.forEach(m -> {
                m.setOwner(gitRepoOwner);
            });
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
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
            throw new JsonParseException();
        }
        return BranchesVo.builder()
                .branches(branches)
                .build();
    }
}
