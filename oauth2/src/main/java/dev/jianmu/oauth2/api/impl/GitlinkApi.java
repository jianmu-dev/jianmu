package dev.jianmu.oauth2.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.exception.*;
import dev.jianmu.oauth2.api.impl.dto.gitlink.LoggingDto;
import dev.jianmu.oauth2.api.impl.dto.gitlink.WebhookCreatingDto;
import dev.jianmu.oauth2.api.impl.dto.gitlink.WebhookUpdatingDto;
import dev.jianmu.oauth2.api.impl.vo.gitlink.*;
import dev.jianmu.oauth2.api.vo.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author huangxi
 * @class GitlinkApi
 * @description GitlinkApi
 * @create 2021-06-30 14:08
 */
@Component
public class GitlinkApi implements OAuth2Api {
    private final static ObjectMapper MAPPER = new ObjectMapper();
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
    public ITokenVo getAccessToken(String code, String redirectUri) {
        // 封装请求条件
        LoggingDto gitlinkLoginVo = LoggingDto.builder()
                .client_id(this.oAuth2Properties.getGitlink().getClientId())
                .client_secret(this.oAuth2Properties.getGitlink().getClientSecret())
                .code(code)
                .grant_type(this.oAuth2Properties.getGitlink().getGrantType())
                .redirect_uri(redirectUri)
                .build();

        String gitlinkLoginJson;
        try {
            gitlinkLoginJson = MAPPER.writeValueAsString(gitlinkLoginVo);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
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
        } catch (HttpServerErrorException e2) {
            throw new HttpServerException();
        }

        TokenVo gitlinkTokenVo;
        try {
            gitlinkTokenVo = MAPPER.readValue(tokenEntity.getBody(), TokenVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return gitlinkTokenVo;
    }

    @Override
    public IUserInfoVo getUserInfo(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer");
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(headers);

        String userTokenInfoUrl = this.oAuth2Properties.getGitlink().getApiUrl() + "users/me.json" + "?access_token=" + token;

        ResponseEntity<String> userInfoEntity;
        try {
            userInfoEntity = this.restTemplate.exchange(
                    userTokenInfoUrl,
                    HttpMethod.GET,
                    param,
                    String.class);
        } catch (HttpClientErrorException e) {
            throw new AccessTokenDoesNotExistException();
        } catch (HttpServerErrorException e2) {
            throw new HttpServerException();
        }

        String userInfo = userInfoEntity.getBody();
        UserInfoVo gitlinkUserInfoVo;
        try {
            gitlinkUserInfoVo = MAPPER.readValue(userInfo, UserInfoVo.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        return gitlinkUserInfoVo;
    }

    @Override
    public IRepoVo getRepo(String accessToken, String gitRepo, String gitRepoOwner) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(
                    this.oAuth2Properties.getGitlink().getApiUrl() + gitRepoOwner + "/" + gitRepo + "/detail",
                    HttpMethod.GET,
                    entity,
                    String.class);
        } catch (HttpClientErrorException clientErrorExceptione) {
            throw new HttpClientException();
        } catch (HttpServerErrorException serverErrorException) {
            throw new HttpServerException();
        }
        RepoVo gitlinkRepoVo;

        try {
            gitlinkRepoVo = MAPPER.readValue(responseEntity.getBody(), RepoVo.class);
            if (gitlinkRepoVo.getStatus() == HttpStatus.FORBIDDEN.value()) {
                throw new NoPermissionException(gitlinkRepoVo.getMessage());
            }
            if (gitlinkRepoVo.getStatus() != 1) {
                throw new UnknownException(gitlinkRepoVo.getMessage());
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return gitlinkRepoVo;
    }

    @Override
    public List<? extends IRepoMemberVo> getRepoMembers(String accessToken, String gitRepo, String gitRepoOwner) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(
                    this.oAuth2Properties.getGitlink().getApiUrl()
                            + gitRepoOwner + "/" + gitRepo +
                            "/collaborators.json?page=1&limit=1000",
                    HttpMethod.GET,
                    entity,
                    String.class);
        } catch (HttpClientErrorException clientErrorExceptione) {
            throw new HttpClientException();
        } catch (HttpServerErrorException serverErrorException) {
            throw new HttpServerException();
        }

        RepoMembersVo gitlinkRepoMemberVo;
        try {
            gitlinkRepoMemberVo = MAPPER.readValue(responseEntity.getBody(), RepoMembersVo.class);
            if (gitlinkRepoMemberVo.getStatus() != null) {
                if (gitlinkRepoMemberVo.getStatus() == HttpStatus.FORBIDDEN.value()) {
                    throw new NoPermissionException(gitlinkRepoMemberVo.getMessage());
                }
                throw new UnknownException(gitlinkRepoMemberVo.getMessage());
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return gitlinkRepoMemberVo.getMembers();
    }

    @Override
    public IBranchesVo getBranches(String accessToken, String gitRepo, String gitRepoOwner) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(
                    this.oAuth2Properties.getGitlink().getApiUrl()
                            + gitRepoOwner + "/" + gitRepo +
                            "/branches.json",
                    HttpMethod.GET,
                    entity,
                    String.class);
        } catch (HttpClientErrorException clientErrorExceptione) {
            throw new HttpClientException();
        } catch (HttpServerErrorException serverErrorException) {
            throw new HttpServerException();
        }
        List<BranchesVo.Branch> branches = null;
        try {
            BranchesVo branchesVo = MAPPER.readValue(responseEntity.getBody(), BranchesVo.class);
            if (branchesVo.getStatus() != null) {
                if (branchesVo.getStatus() == HttpStatus.FORBIDDEN.value()) {
                    throw new NoPermissionException(branchesVo.getMessage());
                }
                throw new UnknownException(branchesVo.getMessage());
            }
        } catch (JsonProcessingException e) {
            try {
                branches = MAPPER.readValue(responseEntity.getBody(), new TypeReference<>() {
                });
            } catch (JsonProcessingException ex) {
                throw new JsonParseException(e.getMessage());
            }
        }
        return BranchesVo.builder()
                .branches(branches).build();
    }

    @Override
    public String getEntryUrl(String owner, String ref) {
        return oAuth2Properties.getGitlink().getBaseUrl() + owner + "/" + ref + "/devops";
    }

    @Override
    public IWebhookVo createWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active) {
        String createWebhookUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + gitRepoOwner + "/" + gitRepo +
                "/webhooks.json";

        WebhookCreatingDto webhookCreatingDto = WebhookCreatingDto.builder()
                .active(active)
                .url(url)
                .build();

        String webhookCreatingJson;
        try {
            webhookCreatingJson = MAPPER.writeValueAsString(webhookCreatingDto);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(webhookCreatingJson, headers);

        // 发送请求, 创建webhook
        ResponseEntity<String> webhookEntity;
        try {
            webhookEntity = this.restTemplate.exchange(createWebhookUrl, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientException("请求失败，客户端发生错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new HttpServerException("请求失败，服务器端发生错误: " + e.getMessage());
        }

        WebhookVo webhookVo;
        try {
            webhookVo = MAPPER.readValue(webhookEntity.getBody(), WebhookVo.class);
            if (webhookVo.getStatus() != null) {
                throw new UnknownException(webhookVo.getMessage());
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return webhookVo;
    }

    @Override
    public void deleteWebhook(String accessToken, String gitRepoOwner, String gitRepo, String id) {
        String deleteWebhookUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + gitRepoOwner + "/" + gitRepo +
                "/webhooks/" + id + ".json";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送请求, 删除webhook
        ResponseEntity<String> webhookEntity;
        try {
            webhookEntity = this.restTemplate.exchange(deleteWebhookUrl, HttpMethod.DELETE, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientException("请求失败，客户端发生错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new HttpServerException("请求失败，服务器端发生错误: " + e.getMessage());
        }

        try {
            Map<String, Object> deleteWebhookMap = MAPPER.readValue(webhookEntity.getBody(), Map.class);
            if (deleteWebhookMap != null && (Integer) deleteWebhookMap.get("status") != 0) {
                throw new UnknownException((String) deleteWebhookMap.get("message"));
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
    }

    @Override
    public void updateWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active, String id) {
        String updateWebhookUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + gitRepoOwner + "/" + gitRepo +
                "/webhooks/" + id + ".json";

        WebhookUpdatingDto webhookUpdatingDto = WebhookUpdatingDto.builder()
                .active(active)
                .url(url)
                .build();

        String webhookUpdatingJson;
        try {
            webhookUpdatingJson = MAPPER.writeValueAsString(webhookUpdatingDto);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(webhookUpdatingJson, headers);

        // 发送请求, 更新webhook
        ResponseEntity<String> webhookEntity;
        try {
            webhookEntity = this.restTemplate.exchange(updateWebhookUrl, HttpMethod.PATCH, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientException("请求失败，客户端发生错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new HttpServerException("请求失败，服务器端发生错误: " + e.getMessage());
        }

        try {
            Map<String, Object> deleteWebhookMap = MAPPER.readValue(webhookEntity.getBody(), Map.class);
            if (deleteWebhookMap != null && deleteWebhookMap.get("status") != null) {
                throw new UnknownException((String) deleteWebhookMap.get("message"));
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
    }

    @Override
    public IWebhookVo getWebhook(String accessToken, String gitRepoOwner, String gitRepo, String id) {
        String getWebhookUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + gitRepoOwner + "/" + gitRepo +
                "/webhooks/" + id + ".json";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送请求, 创建webhook
        ResponseEntity<String> webhookEntity;
        try {
            webhookEntity = this.restTemplate.exchange(getWebhookUrl, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientException("请求失败，客户端发生错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new HttpServerException("请求失败，服务器端发生错误: " + e.getMessage());
        }

        WebhookVo webhookVo;
        try {
            webhookVo = MAPPER.readValue(webhookEntity.getBody(), WebhookVo.class);
            if (webhookVo.getStatus() != null) {
                throw new UnknownException(webhookVo.getMessage());
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return webhookVo;
    }
}
