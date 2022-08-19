package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.git.repo.event.GitRepoDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Daihw
 * @class GitRepoEventHandler
 * @description GitRepoEventHandler
 * @create 2022/8/18 5:56 下午
 */
@Component
public class GitRepoEventHandler {
    private final GitRepoApplication gitRepoApplication;

    public GitRepoEventHandler(
            GitRepoApplication gitRepoApplication
    ) {
        this.gitRepoApplication = gitRepoApplication;
    }

    @EventListener
    @Async
    public void handlerDeletedEvent(GitRepoDeletedEvent event) {
        this.gitRepoApplication.deleteGitRepoData(event.getId(), event.getProjectIds());
    }
}
