package dev.jianmu.infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @class FileSystemStorageService
 * @description 文件系统实现类
 * @author Ethan Liu
 * @create 2021-04-05 21:01
*/
@Service
public class FileSystemStorageService implements StorageService, ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);
    private static final String LogfilePostfix = ".log";

    private final Path rootLocation;

    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLogfilePath());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.init();
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (FileAlreadyExistsException e) {
            logger.info("the directory already exits");
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public BufferedWriter writeLog(String LogFileName) {
        try {
            return new BufferedWriter(
                    new FileWriter(this.rootLocation + File.separator + LogFileName + LogfilePostfix, StandardCharsets.UTF_8, true)
            );
        } catch (IOException e) {
            throw new StorageException("Could not create log file", e);
        }
    }

    @Override
    public BufferedReader readLog(String LogFileName) {
        try {
            return new BufferedReader(
                    new FileReader(this.rootLocation + File.separator + LogFileName + LogfilePostfix, StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            throw new StorageFileNotFoundException("Could not find log file", e);
        }
    }

    @Override
    public File logFile(String LogFileName) {
        return new File(this.rootLocation + File.separator + LogFileName + LogfilePostfix);
    }

    @Override
    public File workflowLogFile(String LogFileName) {
        return new File("workflow_log" + File.separator + LogFileName + LogfilePostfix);
    }
}
