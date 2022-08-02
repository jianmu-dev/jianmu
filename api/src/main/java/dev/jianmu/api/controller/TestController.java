package dev.jianmu.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.dto.impl.GitlinkSilentLoggingDto;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.util.AESEncryptionUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author huangxi
 * @class TemController
 * @description TemController
 * @create 2022-07-29 11:33
 */
@RestController
@RequestMapping("auth/oauth2")
@Tag(name = "oauth2控制器", description = "oauth2控制器")
// TODO 测试代码待删除
public class TestController {
    private final OAuth2Properties oAuth2Properties;

    public TestController(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    @PostMapping("/login/silent/git_repo/code")
    public String authenticateUser(@RequestBody @Valid GitlinkSilentLoggingDto dto) {
        ObjectMapper mapper = new ObjectMapper();
        String code;
        try {
            code = mapper.writeValueAsString(dto);
            code = AESEncryptionUtil.encryptWithIv(code, this.oAuth2Properties.getGitlink().getSilentLogin().getKey(), this.oAuth2Properties.getGitlink().getSilentLogin().getIv());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return code;
    }
}