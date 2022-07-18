package dev.jianmu.application.service.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {
    private Object header;
    private Object body;
    private Object query;
}
