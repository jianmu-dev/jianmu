# Jianmu

#### 介绍

建木自动化集成平台

以触发器、流程流转、任务分发等核心功能为平台核心，可以用在各类使用场景下，

包括但不限于，CI/CD、DevOps、自动化运维、多业务系统集成等使用场景。

#### 架构设计

可以参考 [这里](https://gitee.com/jianmu_dev/jianmu-architecture-as-code)

#### 运行环境

JDK 11 及以上

Docker 18.09.7及以上

Mysql 8.0及以上

go 1.15及以上

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