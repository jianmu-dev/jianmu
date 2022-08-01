package dev.jianmu.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @class ManualEvent
 * @description ManualEvent
 * @author Ethan Liu
 * @create 2021-11-18 15:37
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManualEvent {
    private String projectId;
    private String associationId;
    private String associationType;
    private String userId;
    private String encryptedToken;
}
