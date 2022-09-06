# 建木CI

#### 介绍

> 建木持续集成平台基于建木自动化平台作为核心实现的持续集成平台，为开发者构建便捷的编译、测试、打包、发布、部署和通知等持续集成生命周期中的相关服务。

![](https://jianmu-blog.assets.dghub.cn/jianmu-blog/1.29.0/assets/blog-source/%E7%AC%AC%E4%B8%80%E5%B1%8F%E5%9B%BE.png)

[ci.jianmu.dev](https://ci.jianmu.dev)

#### TL;DR

```shell
wget https://gitee.com/jianmu-dev/jianmu-deploy/raw/master/docker-compose.yml

docker-compose up -d
```

#### 运行环境

* JDK 11 及以上
* Mysql 8.0及以上

#### 如何编译

`mvn package`

#### 如何运行

参考 [application.yml](https://gitee.com/jianmu-dev/jianmu/blob/master/api/src/main/resources/application.yml) 中的配置创建你自己的 `application-dev.yml` 配置文件来覆盖需要配置的值，如datasource.url（当前必须使用名为dev的profile）。

**配置admin用户的密码：**

```yaml
jianmu:
  api:
    adminPasswd: 123456
```

**[配置Hub](https://jianmuhub.com/user-center/api-key) 的AK/SK：**

```yaml
registry:
  ak: 703a46428d8f411c9f3233a53af56749
  sk: 8db2979bcc964c95921d18ce8a0c1e1e
```

**配置Worker：**
> 注意：该配置从版本v2.5.0开始支持

```yaml
jianmu:
  worker:
    secret: worker-secret
```

可以使用 openssl 生成密钥

```shell
openssl rand -hex 16
```

这部分配置是用来验证Worker与服务器的连接，Worker和服务器必须配置相同的密钥值。

[官方示例](https://ci.jianmu.dev)

[快速开始](https://docs.jianmu.dev/guide/quick-start.html)

[官网首页](https://jianmu.dev)