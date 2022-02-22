package dev.jianmu.trigger.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class WebhookRequest
 * @description Webhook请求
 * @author Ethan Liu
 * @create 2021-11-14 21:26
 */
public class WebRequest {
    public enum StatusCode {
        OK,
        NOT_ACCEPTABLE,
        UNAUTHORIZED,
        NOT_FOUND,
        ALREADY_RUNNING,
        UNKNOWN
    }

    private String id;
    private String projectId;
    private String workflowRef;
    private String workflowVersion;
    private String triggerId;
    private String userAgent;
    private String payload;
    private StatusCode statusCode;
    private String errorMsg;
    private LocalDateTime requestTime;

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setWorkflowRef(String workflowRef) {
        this.workflowRef = workflowRef;
    }

    public void setWorkflowVersion(String workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getPayload() {
        return payload;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public static final class Builder {
        private String userAgent;
        private String payload;
        private StatusCode statusCode;
        private String errorMsg;

        private Builder() {
        }

        public static Builder aWebRequest() {
            return new Builder();
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder statusCode(StatusCode statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder errorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
            return this;
        }

        public WebRequest build() {
            WebRequest webRequest = new WebRequest();
            webRequest.id = UUID.randomUUID().toString().replace("-", "");
            webRequest.statusCode = this.statusCode;
            webRequest.errorMsg = this.errorMsg;
            webRequest.payload = this.payload;
            webRequest.userAgent = this.userAgent;
            webRequest.requestTime = LocalDateTime.now();
            return webRequest;
        }
    }
}
