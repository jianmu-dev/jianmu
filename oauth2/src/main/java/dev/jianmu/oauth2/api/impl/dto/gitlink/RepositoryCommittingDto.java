package dev.jianmu.oauth2.api.impl.dto.gitlink;

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
    private final String author_email = "1014@qq.com";
    private final String author_name = "hx12332112345";
    private final String committer_email = "1014@qq.com";
    private final String committer_name = "hx12332112345";
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
