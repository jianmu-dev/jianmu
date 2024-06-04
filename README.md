# 建木

<div>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://gitee.com/jianmu-dev/jianmu/badge/star.svg?theme=gvp' alt='gitee star'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu/blob/master/LICENSE">
        <img src='https://img.shields.io/badge/liscense-MulanPSL--2.0-green.svg' alt='license'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/OS%2FARCH-AMD64%2FARM64-important.svg' alt='os/arch'/>
    </a>
</div>
<div>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/-%E6%97%A0%E4%BB%A3%E7%A0%81(%E5%9B%BE%E5%BD%A2%E5%8C%96)-brightgreen.svg' alt='无代码(图形化)'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/-%E4%BD%8E%E4%BB%A3%E7%A0%81(GitOps)-brightgreen.svg' alt='低代码(GitOps)'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/devops-yellow.svg' alt='devops'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/CI%2FCD%2FCO-yellow.svg' alt='ci/cd/co'/>
    </a>
</div>
<div>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/JDK11+-lightgrey.svg' alt='jdk'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/MySQL8+-lightgrey.svg' alt='mysql'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/Vue3-lightgrey.svg' alt='vue'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/Typescript4+-lightgrey.svg' alt='typescript'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/Docker-lightgrey.svg' alt=docker'/>
    </a>
    <a target="_blank" href="https://gitee.com/jianmu-dev/jianmu">
        <img src='https://img.shields.io/badge/Kubernetes-lightgrey.svg' alt=kubernetes'/>
    </a>
</div>

#### 介绍

> 建木是一个面向DevOps领域的极易扩展的开源无代码(图形化)/低代码(GitOps)工具。可以帮助用户轻松编排各种DevOps流程并分发到不同平台执行。

![](https://jianmu-blog.assets.dghub.cn/jianmu-blog/1.80.1/assets/blog-source/%E7%AC%AC%E4%B8%80%E5%B1%8F%E5%9B%BE.png)

在线体验：[devops.jianmuhub.com](https://devops.jianmuhub.com)

开源示例：[ci.jianmu.dev](https://ci.jianmu.dev)

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

**问题反馈：**

若，使用过程中遇到问题，开issue反馈给我们。

* 建木：https://gitee.com/jianmu-dev/jianmu/issues
* 建木Worker：https://gitee.com/organizations/jianmu-workers/issues
* 建木节点：https://gitee.com/organizations/jianmu-runners/issues

[官方示例](https://ci.jianmu.dev)

[建木文档](https://docs.jianmu.dev)

[建木官网](https://jianmu.dev)

![联系我们](./contact.png)