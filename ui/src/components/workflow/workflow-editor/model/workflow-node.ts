import { IWorkflowNode } from './data/common';
import { Cron } from './data/node/cron';
import { Webhook } from './data/node/webhook';
import { Shell } from './data/node/shell';
import { AsyncTask } from './data/node/async-task';

export default class WorkflowNode {

  constructor() {
  }

  search(keyword?: string): IWorkflowNode[][] {
    return [
      this.loadInnerTriggers(keyword),
      this.loadInnerNodes(keyword),
      this.loadLocalNodes(keyword),
      this.loadOfficialNodes(keyword),
      this.loadCommunityNodes(keyword),
    ];
  }

  private loadInnerTriggers(keyword?: string): IWorkflowNode[] {
    const arr: IWorkflowNode[] = [new Cron(), new Webhook()];

    return keyword ? arr.filter(item => item.getName().includes(keyword)) : arr;
  }

  private loadInnerNodes(keyword?: string): IWorkflowNode[] {
    const arr: IWorkflowNode[] = [new Shell()];

    return keyword ? arr.filter(item => item.getName().includes(keyword)) : arr;

  }

  private loadLocalNodes(keyword?: string): IWorkflowNode[] {
    // TODO 加载本地节点
    return [];
  }

  private loadOfficialNodes(keyword?: string): IWorkflowNode[] {
    // TODO 加载hub官方节点
    const arr: IWorkflowNode[] = [
      new AsyncTask('_/git_clone', '克隆建木CI代码', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FikR5g_gILRZjr-olpMqypjhfuj3'),
      new AsyncTask('_/nodejs_build', 'NodeJs构建前端项目', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FpON0edVLhS5j3Kgvs9i-rwljruu'),
      new AsyncTask('_/docker_image_build', 'docker镜像构建', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FvWtndEdOK9WmEc8WCmvKLYpy2Xv'),
      new AsyncTask('_/npm_publish', '发布npm依赖包', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FtRbpLVb0vl5qURYdyxMAHE8c7tT'),
      new AsyncTask('_/org_gov', '组织治理', 'https://jianmuhub.img.dghub.cn/node-definition/icon/FlENvzR04GwGJMgUvC_UGadygwXl'),
      new AsyncTask('_/test', '测试节点', 'https://jianmu-hub.assets.dghub.cn/jianmu-hub-ui/1.6.21/assets/default.d4db0995.svg'),
    ];

    return keyword ? arr.filter(item => item.getName().includes(keyword)) : arr;
  }

  private loadCommunityNodes(keyword?: string): IWorkflowNode[] {
    // TODO 加载hub社区节点
    return [];

  }
}