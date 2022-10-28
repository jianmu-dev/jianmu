package dev.jianmu.infrastructure.mybatis.git_repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.git.repo.aggregate.User;
import dev.jianmu.git.repo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author Daihw
 * @class UserRepositoryImpl
 * @description UserRepositoryImpl
 * @create 2022/10/27 10:29 上午
 */
@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final static ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final RestTemplate restTemplate;
    @Value("${jianmu.oauth2.gitlink.engine-address}")
    private String engineAddress;

    public UserRepositoryImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public User getByUsername(String username) {
        try {
            return this.restTemplate.exchange(this.engineAddress + "gitlink_engine/users/" + username, HttpMethod.GET, null, User.class).getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            var message = e.getResponseBodyAsString();
            try {
                message = MAPPER.readValue(message, new TypeReference<Map<String, String>>() {
                }).get("message");
            } catch (JsonProcessingException ignore) {
            }
            throw new RuntimeException("未找到用户：" + message, e);
        }
    }
}
