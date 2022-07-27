package dev.jianmu.oauth2.api.impl.vo.gitlink;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.oauth2.api.vo.IWebhookVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class WebhookVo
 * @description WebhookVo
 * @create 2022-07-26 16:55
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookVo implements IWebhookVo {
    @JsonProperty("id")
    private String _id;
    private Integer status;
    private String message;

    @JsonIgnore
    @Override
    public String getId() {
        return this._id;
    }
}
