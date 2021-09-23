package dev.jianmu.worker.aggregate;

/**
 * @class: WorkerCommands
 * @description: WorkerCommands
 * @author: Ethan Liu
 * @create: 2021-09-10 22:32
 **/
public interface WorkerCommands {
    void createWorkspace(String workspaceName);

    void deleteWorkspace(String workspaceName);

    void runTask(WorkerTask workerTask);
}
