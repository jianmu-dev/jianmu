package dev.jianmu.oauth2.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.exception.*;
import dev.jianmu.oauth2.api.impl.dto.gitlink.LoggingDto;
import dev.jianmu.oauth2.api.impl.dto.gitlink.RepositoryCommittingDto;
import dev.jianmu.oauth2.api.impl.dto.gitlink.WebhookCreatingDto;
import dev.jianmu.oauth2.api.impl.dto.gitlink.WebhookUpdatingDto;
import dev.jianmu.oauth2.api.impl.vo.gitlink.*;
import dev.jianmu.oauth2.api.vo.*;
import lombok.Builder;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author huangxi
 * @class GitlinkApi
 * @description GitlinkApi
 * @create 2021-06-30 14:08
 */
@Builder
public class GitlinkApi implements OAuth2Api {
    private final static ObjectMapper MAPPER = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final OAuth2Properties oAuth2Properties;
    private final String userId;

    public GitlinkApi(RestTemplate restTemplate, OAuth2Properties oAuth2Properties, String userId) {
        this.restTemplate = restTemplate;
        this.oAuth2Properties = oAuth2Properties;
        this.userId = userId;
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

        String grantType = this.oAuth2Properties.getGitlink().getGrantType();
        LoggingDto gitlinkLoginVo;
        if (grantType.equals("client_credentials")) {
            gitlinkLoginVo = LoggingDto.builder()
                    .clientId(this.oAuth2Properties.getGitlink().getClientId())
                    .clientSecret(this.oAuth2Properties.getGitlink().getClientSecret())
                    .grantType(grantType)
                    .build();
        } else {
            gitlinkLoginVo = LoggingDto.builder()
                    .clientId(this.oAuth2Properties.getGitlink().getClientId())
                    .clientSecret(this.oAuth2Properties.getGitlink().getClientSecret())
                    .code(code)
                    .grantType(grantType)
                    .redirectUri(redirectUri)
                    .build();
        }
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
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(headers);

        String userTokenInfoUrl = this.oAuth2Properties.getGitlink().getApiUrl() + "users/me.json";
        if (this.userId != null) {
            userTokenInfoUrl += "?uid=" + this.userId;
        }

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

        String getRepoUrl = this.oAuth2Properties.getGitlink().getApiUrl() + gitRepoOwner + "/" + gitRepo + "/detail";
        if (this.userId != null) {
            getRepoUrl += "?uid=" + this.userId;
        }

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(
                    getRepoUrl,
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
            if (gitlinkRepoVo.getStatus() == 404) {
                throw new RepoExistedException(gitlinkRepoVo.getMessage());
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

        String getRepoMembersUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + gitRepoOwner + "/" + gitRepo +
                "/collaborators.json?page=1&limit=1000";
        if (this.userId != null) {
            getRepoMembersUrl += "&uid=" + this.userId;
        }

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(
                    getRepoMembersUrl,
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

        String getBranchesUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + gitRepoOwner + "/" + gitRepo +
                "/branches.json";
        if (this.userId != null) {
            getBranchesUrl += "?uid=" + this.userId;
        }

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(
                    getBranchesUrl,
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
    public IWebhookVo createWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active, List<String> events) {
        String createWebhookUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + gitRepoOwner + "/" + gitRepo +
                "/webhooks.json";
        if (this.userId != null) {
            createWebhookUrl += "?uid=" + this.userId;
        }

        WebhookCreatingDto webhookCreatingDto = WebhookCreatingDto.builder()
                .active(active)
                .url(url)
                .events(events)
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
        if (this.userId != null) {
            deleteWebhookUrl += "?uid=" + this.userId;
        }

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
    public void updateWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active, String id, List<String> events) {
        String updateWebhookUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + gitRepoOwner + "/" + gitRepo +
                "/webhooks/" + id + ".json";
        if (this.userId != null) {
            updateWebhookUrl += "?uid=" + this.userId;
        }

        WebhookUpdatingDto webhookUpdatingDto = WebhookUpdatingDto.builder()
                .active(active)
                .url(url)
                .events(events)
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
        if (this.userId != null) {
            getWebhookUrl += "?uid=" + this.userId;
        }

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

    @Override
    public void createFile(String accessToken, String owner, String repo,
                           String content, String filepath,
                           String authorEmail, String authorName, String committerEmail, String committerName,
                           String branch, String message) {

        String createFileUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + owner + "/" + repo +
                "/contents/batch" + ".json";
        if (this.userId != null) {
            createFileUrl += "?uid=" + this.userId;
        }

        long timeunix = new Date().getTime() / 1000;
        ArrayList<RepositoryCommittingDto.File> files = new ArrayList<>();
        files.add(RepositoryCommittingDto.File.builder()
                .actionType("create")
                .filePath(filepath)
                .content(content)
                .build());
        RepositoryCommittingDto repositoryCommittingDto = RepositoryCommittingDto.builder()
                .authorEmail(authorEmail)
                .authorName(authorName)
                .committerEmail(committerEmail)
                .committerName(committerName)
                .authorTimeunix(timeunix)
                .committerTimeunix(timeunix)
                .branch(branch)
                .message(message)
                .files(files)
                .build();

        String repositoryCommittingJson;
        try {
            repositoryCommittingJson = MAPPER.writeValueAsString(repositoryCommittingDto);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(repositoryCommittingJson, headers);

        ResponseEntity<String> fileEntity;
        try {
            fileEntity = this.restTemplate.exchange(createFileUrl, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientException("请求失败，客户端发生错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new HttpServerException("请求失败，服务器端发生错误: " + e.getMessage());
        }

        try {
            Map<String, Object> createFileMap = MAPPER.readValue(fileEntity.getBody(), Map.class);
            if (createFileMap != null && createFileMap.get("status") != null) {
                throw new UnknownException((String) createFileMap.get("message"));
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
    }

    @Override
    public void deleteFile(String accessToken, String owner, String repo,
                           String content, String filepath,
                           String authorEmail, String authorName, String committerEmail, String committerName,
                           String branch, String message) {
        String deleteFileUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + owner + "/" + repo +
                "/contents/batch" + ".json";
        if (this.userId != null) {
            deleteFileUrl += "?uid=" + this.userId;
        }

        long timeunix = new Date().getTime() / 1000;
        ArrayList<RepositoryCommittingDto.File> files = new ArrayList<>();
        files.add(RepositoryCommittingDto.File.builder()
                .actionType("delete")
                .filePath(filepath)
                .content(content)
                .build());
        RepositoryCommittingDto repositoryCommittingDto = RepositoryCommittingDto.builder()
                .authorEmail(authorEmail)
                .authorName(authorName)
                .committerEmail(committerEmail)
                .committerName(committerName)
                .authorTimeunix(timeunix)
                .committerTimeunix(timeunix)
                .branch(branch)
                .message(message)
                .files(files)
                .build();

        String repositoryCommittingJson;
        try {
            repositoryCommittingJson = MAPPER.writeValueAsString(repositoryCommittingDto);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(repositoryCommittingJson, headers);

        ResponseEntity<String> fileEntity;
        try {
            fileEntity = this.restTemplate.exchange(deleteFileUrl, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientException("请求失败，客户端发生错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new HttpServerException("请求失败，服务器端发生错误: " + e.getMessage());
        }

        try {
            Map<String, Object> deleteFileMap = MAPPER.readValue(fileEntity.getBody(), Map.class);
            if (deleteFileMap != null && deleteFileMap.get("status") != null) {
                throw new UnknownException((String) deleteFileMap.get("message"));
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
    }

    @Override
    public void updateFile(String accessToken, String owner, String repo,
                           String content, String filepath,
                           String authorEmail, String authorName, String committerEmail, String committerName,
                           String branch, String message) {

        String updateFileUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + "v1/" + owner + "/" + repo +
                "/contents/batch" + ".json";
        if (this.userId != null) {
            updateFileUrl += "?uid=" + this.userId;
        }

        long timeunix = new Date().getTime() / 1000;
        ArrayList<RepositoryCommittingDto.File> files = new ArrayList<>();
        files.add(RepositoryCommittingDto.File.builder()
                .actionType("update")
                .filePath(filepath)
                .content(content)
                .build());
        RepositoryCommittingDto repositoryCommittingDto = RepositoryCommittingDto.builder()
                .authorEmail(authorEmail)
                .authorName(authorName)
                .committerEmail(committerEmail)
                .committerName(committerName)
                .authorTimeunix(timeunix)
                .committerTimeunix(timeunix)
                .branch(branch)
                .message(message)
                .files(files)
                .build();

        String repositoryCommittingJson;
        try {
            repositoryCommittingJson = MAPPER.writeValueAsString(repositoryCommittingDto);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(repositoryCommittingJson, headers);

        ResponseEntity<String> fileEntity;
        try {
            fileEntity = this.restTemplate.exchange(updateFileUrl, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientException("请求失败，客户端发生错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new HttpServerException("请求失败，服务器端发生错误: " + e.getMessage());
        }

        try {
            Map<String, Object> updateFileMap = MAPPER.readValue(fileEntity.getBody(), Map.class);
            if (updateFileMap != null && updateFileMap.get("status") != null) {
                throw new UnknownException((String) updateFileMap.get("message"));
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
    }

    @Override
    public IFileVo getFile(String accessToken, String owner, String repo, String filepath, String ref) {
        String getFileUrl = this.oAuth2Properties.getGitlink().getApiUrl()
                + owner + "/" + repo + "/sub_entries" + ".json"
                + "?ref=" + ref + "&filepath=" + filepath;
        if (this.userId != null) {
            getFileUrl += "&uid=" + this.userId;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> fileEntity;
        try {
            fileEntity = this.restTemplate.exchange(getFileUrl, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientException("请求失败，客户端发生错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new HttpServerException("请求失败，服务器端发生错误: " + e.getMessage());
        }
        FileVo file;
        try {
            file = MAPPER.readValue(fileEntity.getBody(), FileVo.class);
            if (file != null && file.getStatus() != null) {
                throw new UnknownException(file.getMessage());
            }
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }
        return file;
    }

    @Override
    public boolean checkOrganizationMember(String accessToken, String org, String userId, String username) {
        return false;
    }
}
