package dev.jianmu.infrastructure.jgit;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import dev.jianmu.project.aggregate.GitRepo;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class: JgitService
 * @description: JgitService
 * @author: Ethan Liu
 * @create: 2021-05-13 15:40
 **/
@Service
public class JgitService {
    private static final Logger logger = LoggerFactory.getLogger(JgitService.class);
    private static final String TMPDIR = "/tmp/";

    private Map<String, Boolean> listFile(File directory) {
        var files = directory.listFiles();
        if (files != null) {
            return Arrays.stream(files)
                    .map(file -> Map.entry(file.getName(), file.isDirectory()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return Map.of();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                return false;
            }
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public void cleanUp(String gitRepoId) {
        File directory = new File("/tmp/" + gitRepoId);
        var res = this.deleteDir(directory);
        logger.info("Git缓存文件清除结果为: {}", res);
    }

    public String readDsl(String gitRepoId, String dslPath) {
        try (FileReader fileReader = new FileReader("/tmp/" + gitRepoId + "/" + dslPath, StandardCharsets.UTF_8)) {
            String dslText = IOUtils.toString(fileReader);
            fileReader.close();
            return dslText;
        } catch (IOException e) {
            logger.error("读取DSL异常：", e);
            throw new RuntimeException("读取DSL异常");
        }
    }

    public Map<String, Boolean> listFiles(String dir) {
        File directory = new File("/tmp/" + dir);
        return this.listFile(directory);
    }

    public void cloneRepoWithUserAndPass(GitRepo gitRepo, String user, String pass) {
        File directory = new File(TMPDIR + gitRepo.getId());
        try (
                Git newlyCloned = Git.cloneRepository()
                        .setDirectory(directory)
                        .setURI(gitRepo.getUri())
                        .setBranch(gitRepo.getBranch())
                        .setTransportConfigCallback(transport -> {
                            if (transport instanceof TransportHttp) {
                                ((TransportHttp) transport).setPreemptiveBasicAuthentication(user, pass);
                            }
                        }).call()
        ) {
            logger.info("Clone Git Repo: {} 成功", gitRepo.getUri());
        } catch (GitAPIException e) {
            logger.error("Clone Failed:", e);
            throw new RuntimeException("克隆失败");
        }
    }


    public void cloneRepoWithSshKey(GitRepo gitRepo, String sshKey) {
        File directory = new File(TMPDIR + gitRepo.getId());
        try (
                Git newlyCloned = Git.cloneRepository()
                        .setDirectory(directory)
                        .setURI(gitRepo.getUri())
                        .setBranch(gitRepo.getBranch())
                        .setTransportConfigCallback(transport -> {
                            if (transport instanceof SshTransport) {
                                ((SshTransport) transport).setSshSessionFactory(new JschConfigSessionFactory() {
                                    @Override
                                    protected void configure(OpenSshConfig.Host hc, Session session) {
                                        session.setConfig("StrictHostKeyChecking", "no");
                                    }

                                    @Override
                                    protected JSch createDefaultJSch(FS fs) throws JSchException {
                                        JSch sch = super.createDefaultJSch(fs);
                                        byte[] prvKey = sshKey.getBytes(StandardCharsets.UTF_8);
                                        sch.addIdentity("gitKey", prvKey, null, null);
                                        return sch;
                                    }
                                });
                            }
                        }).call()
        ) {
            logger.info("Clone Git Repo: {} 成功", gitRepo.getUri());
        } catch (GitAPIException e) {
            logger.error("Clone Failed:", e);
            throw new RuntimeException("克隆失败");
        }
    }

    public void cloneRepo(GitRepo gitRepo) {
        File directory = new File(TMPDIR + gitRepo.getId());
        try (
                Git newlyCloned = Git.cloneRepository()
                        .setDirectory(directory)
                        .setURI(gitRepo.getUri())
                        .setBranch(gitRepo.getBranch())
                        .setTransportConfigCallback(transport -> {
                            if (transport instanceof SshTransport) {
                                ((SshTransport) transport).setSshSessionFactory(new JschConfigSessionFactory() {
                                    @Override
                                    protected void configure(OpenSshConfig.Host hc, Session session) {
                                        session.setConfig("StrictHostKeyChecking", "no");
                                    }
                                });
                            }
                        }).call()
        ) {
            logger.info("Clone Git Repo: {} 成功", gitRepo.getUri());
        } catch (GitAPIException e) {
            logger.error("Clone Failed:", e);
            throw new RuntimeException("克隆失败");
        }
    }
}
