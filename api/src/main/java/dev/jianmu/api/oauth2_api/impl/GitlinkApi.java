package dev.jianmu.api.oauth2_api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.oauth2_api.OAuth2Api;
import dev.jianmu.api.oauth2_api.config.OAuth2Properties;
import dev.jianmu.api.oauth2_api.exception.AccessTokenDoesNotExistException;
import dev.jianmu.api.oauth2_api.exception.JsonParseException;
import dev.jianmu.api.oauth2_api.impl.vo.GitlinkTokenVo;
import dev.jianmu.api.oauth2_api.impl.vo.GitlinkUserInfoVo;
import dev.jianmu.api.oauth2_api.vo.UserInfoVo;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
        return null;
    }

    @Override
    public String getAccessToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("grant_type", this.oAuth2Properties.getGitlink().getGrantType());
        map.add("client_id", this.oAuth2Properties.getGitlink().getClientId());
        map.add("client_secret", this.oAuth2Properties.getGitlink().getClientSecret());
        map.add("username", "innov");
        map.add("password", "12345678");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(this.oAuth2Properties.getGitlink().getTokenUrl(), param, String.class);

        ObjectMapper mapper = new ObjectMapper();
        GitlinkTokenVo gitlinkTokenVo;
        try {
            gitlinkTokenVo = mapper.readValue(response.getBody(), GitlinkTokenVo.class);
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
                .headUrl(this.oAuth2Properties.getGitlink().getBaseUrl() + gitlinkUserInfoVo.getImage_url())
                .nickname(gitlinkUserInfoVo.getUsername())
                .username(gitlinkUserInfoVo.getLogin())
                .data(userInfo)
                .build();
    }
}
