package dev.jianmu.infrastructure.mybatis.git_repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dev.jianmu.git.repo.aggregate.Token;
import dev.jianmu.git.repo.repository.AccessTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * @author Daihw
 * @class AccessTokenRepositoryImpl
 * @description AccessTokenRepositoryImpl
 * @create 2022/10/26 1:59 下午
 */
@Repository
public class AccessTokenRepositoryImpl implements AccessTokenRepository {
    private final static ObjectMapper MAPPER = new ObjectMapper();
    private static volatile Token token;

    static {
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Value("${jianmu.oauth2.gitlink.base-url}")
    private String baseUrl;
    @Value("${jianmu.oauth2.gitlink.client-id}")
    private String clientId;
    @Value("${jianmu.oauth2.gitlink.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    public AccessTokenRepositoryImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String get() {
        if (token != null && token.exist()) {
            return token.getAccessToken();
        }
        synchronized (AccessTokenRepositoryImpl.class) {
            if (token != null && token.exist()) {
                return token.getAccessToken();
            }
            var param = new HashMap<String, String>();
            param.put("grant_type", "client_credentials");
            param.put("client_id", this.clientId);
            param.put("client_secret", this.clientSecret);

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            var entity = new HttpEntity<>(param, headers);

            try {
                var responseEntity = this.restTemplate.exchange(this.baseUrl + "oauth/token", HttpMethod.POST, entity, String.class);
                token = MAPPER.readValue(responseEntity.getBody(), Token.class);
            } catch (HttpServerErrorException e) {
                throw new RuntimeException("获取accessToken失败：", e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("accessToken序列化失败：", e);
            }
        }
        return token.getAccessToken();
    }
}
