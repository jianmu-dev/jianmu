package dev.jianmu.infrastructure.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * @class: StorageService
 * @description: 文件存储服务
 * @author: Ethan Liu
 * @create: 2021-04-05 20:37
 **/
public interface StorageService {
    void init();

    BufferedWriter writeLog(String LogFileName);

    BufferedReader readLog(String LogFileName);
}
