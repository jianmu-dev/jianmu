import { INodeData } from './data';
import { Cron } from './node/cron';
import { Webhook } from './node/webhook';
import { Shell } from './node/shell';
import { AsyncTask } from './node/async-task';

export default class WorkflowNode {

  constructor() {
  }

  search(keyword?: string): INodeData[][] {
    return [
      this.loadInnerTriggers(keyword),
      this.loadInnerNodes(keyword),
      this.loadLocalNodes(keyword),
      this.loadOfficialNodes(keyword),
      this.loadCommunityNodes(keyword),
    ];
  }

  private loadInnerTriggers(keyword?: string): INodeData[] {
    const arr: INodeData[] = [new Cron(), new Webhook()];

    return keyword ? arr.filter(item => item.getName().includes(keyword)) : arr;
  }

  private loadInnerNodes(keyword?: string): INodeData[] {
    const arr: INodeData[] = [new Shell()];

    return keyword ? arr.filter(item => item.getName().includes(keyword)) : arr;

  }

  private loadLocalNodes(keyword?: string): INodeData[] {
    // TODO 加载本地节点
    return [];
  }

  private loadOfficialNodes(keyword?: string): INodeData[] {
    // TODO 加载hub官方节点
    const arr: INodeData[] = [
      new AsyncTask('克隆建木CI代码', 'git_clone', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FikR5g_gILRZjr-olpMqypjhfuj3'),
      new AsyncTask('NodeJs构建前端项目', 'node_build', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FpON0edVLhS5j3Kgvs9i-rwljruu'),
      new AsyncTask('docker镜像构建', 'docker_build', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FvWtndEdOK9WmEc8WCmvKLYpy2Xv'),
      new AsyncTask('发布npm依赖包', 'npm_publish', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FtRbpLVb0vl5qURYdyxMAHE8c7tT'),
      new AsyncTask('组织治理', 'org_gov', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FlENvzR04GwGJMgUvC_UGadygwXl'),
      new AsyncTask('测试节点', 'test', ''),
    ];

    return keyword ? arr.filter(item => item.getName().includes(keyword)) : arr;
  }

  private loadCommunityNodes(keyword?: string): INodeData[] {
    // TODO 加载hub社区节点
    return [];

  }
}