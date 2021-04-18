package dev.jianmu.infrastructure.jsonfile;

import dev.jianmu.infrastructure.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @class: JsonRepositoryInit
 * @description: Json文件仓储初始化
 * @author: Ethan Liu
 * @create: 2021-04-18 22:21
 **/
@Component
public class JsonRepositoryInit implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(JsonRepositoryInit.class);
    private final Path rootLocation;
    public static final String POSTFIX = ".json";

    public JsonRepositoryInit(JsonRepositoryProperties properties) {
        this.rootLocation = Paths.get(properties.getRepoPath());
    }

    public Path getRootLocation() {
        return rootLocation;
    }

    private void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (FileAlreadyExistsException e) {
            logger.info("the directory already exits");
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.init();
    }
}
