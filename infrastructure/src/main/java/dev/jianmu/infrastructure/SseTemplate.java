package dev.jianmu.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Ethan Liu
 * @class SseTemplate
 * @description SseTemplate
 * @create 2022-05-11 08:28
 */
@Service
public class SseTemplate {

    public SseEmitter newSseEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        return emitter;
    }

    public void sendMessage(SseEmitter.SseEventBuilder event, SseEmitter sseEmitter) {
        try {
            sseEmitter.send(event);
        } catch (Exception ex) {
            sseEmitter.completeWithError(ex);
        }
    }
}
