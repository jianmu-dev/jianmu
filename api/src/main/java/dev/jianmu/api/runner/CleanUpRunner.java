package dev.jianmu.api.runner;

import dev.jianmu.application.service.ProjectApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Ethan Liu
 * @class CleanUpRunner
 * @description CleanUpRunner
 * @create 2022-01-04 10:10
 */
@Component
@Slf4j
public class CleanUpRunner implements ApplicationRunner {
    private final ProjectApplication projectApplication;

    public CleanUpRunner(ProjectApplication projectApplication) {
        this.projectApplication = projectApplication;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.projectApplication.autoClean();
    }
}
