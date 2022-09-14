package dev.jianmu.oauth2.api.impl.dto.gitlink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author huangxi
 * @class RepositoryCommittingDto
 * @description RepositoryCommittingDto
 * @create 2022-07-27 15:19
 */
@Builder
@Getter
@Setter
public class RepositoryCommittingDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("author_email")
    private String authorEmail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("author_name")
    private String authorName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("committer_email")
    private String committerEmail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("committer_name")
    private String committerName;
    private List<File> files;
    @JsonProperty("author_timeunix")
    private long authorTimeunix;
    @JsonProperty("committer_timeunix")
    private long committerTimeunix;
    private String branch;
    private String message;

    @Builder
    @Getter
    @Setter
    public static class File {
        private final String encoding = "text";
        @JsonProperty("action_type")
        private String actionType;
        private String content;
        @JsonProperty("file_path")
        private String filePath;
    }
}
