package dev.jianmu.infrastructure.client;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @class RestTemplateConfig
 * @description RestTemplateConfig
 * @author Ethan Liu
 * @create 2021-06-21 09:09
*/
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        HttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }
}
