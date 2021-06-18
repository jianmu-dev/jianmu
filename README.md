# Jianmu

#### 介绍

建木CI/CD

基于建木自动化集成平台实现的CI/CD平台

#### 运行环境

JDK 11 及以上

Docker 18.09.7及以上

Mysql 8.0及以上

go 1.15及以上

#### 如何编译

暂缺

#### 如何运行

参考 [application.yml](https://gitee.com/jianmu_dev/jianmu-main/blob/master/api/src/main/resources/application.yml) 中的配置

创建你自己的 `application-dev.yml` 配置文件来覆盖需要配置的值，如datasource.url（当前必须使用名为dev的profile）

注意以下配置：

```yaml
embedded:
  dockerworker:
    docker-host: tcp://127.0.0.1:2375
    api-version: v1.39
```

这部分配置是用来开启服务内置Worker的，docker-host指向的是Docker Engine的地址和端口。

默认Docker Engine是不对外开放的，因此需要自行修改配置。具体可以参考 [Docker Engine API的官方文档](https://docs.docker.com/engine/api/)