# 建木CI

#### 介绍

> 建木持续集成平台基于建木自动化平台作为核心实现的持续集成平台，为开发者构建便捷的编译、测试、打包、发布、部署和通知等持续集成生命周期中的相关服务。

#### TL;DR;

```shell
$ wget https://gitee.com/jianmu-dev/jianmu-deploy/raw/master/docker-compose.yml
$ docker-compose up -d
```

#### 运行环境

* JDK 11 及以上
* Docker 18.09.7及以上
* Mysql 8.0及以上

#### 如何编译

`mvn package`

#### 如何运行

参考 [application.yml](https://gitee.com/jianmu-dev/jianmu-ci-server/blob/master/api/src/main/resources/application.yml) 中的配置创建你自己的 `application-dev.yml` 配置文件来覆盖需要配置的值，如datasource.url（当前必须使用名为dev的profile）。

配置admin用户的密码：

```yaml
jianmu:
  api:
    adminPasswd: 123456
```

[配置Hub](https://hub.jianmu.run/user-center/api-key)的AK/SK：

```yaml
registry:
  ak: 703a46428d8f411c9f3233a53af56749
  sk: 8db2979bcc964c95921d18ce8a0c1e1e
```

配置docker API的地址：

```yaml
embedded:
  dockerworker:
    docker-host: tcp://127.0.0.1:2375
    api-version: v1.39
```

这部分配置是用来开启服务内置Worker的，docker-host指向的是Docker Engine的地址和端口。

默认Docker Engine是不对外开放的，因此需要自行修改配置。具体可以参考 [Docker Engine API的官方文档](https://docs.docker.com/engine/api/)

[详见官方示例](https://ci.jianmu.dev)

[快速开始](https://docs.jianmu.dev/guide/quick-start.html)

[官网首页](https://jianmu.dev)