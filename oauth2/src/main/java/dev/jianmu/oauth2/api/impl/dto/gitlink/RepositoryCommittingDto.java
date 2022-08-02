package dev.jianmu.oauth2.api.impl.dto.gitlink;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String author_email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String author_name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String committer_email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String committer_name;
    private List<File> files;
    private long author_timeunix;
    private long committer_timeunix;
    private String branch;
    private String message;

    @Builder
    @Getter
    @Setter
    public static class File {
        private final String encoding = "text";
        private String action_type;
        private String content;
        private String file_path;
    }
}
