import { INodeData } from './data';
import { NodeTypeEnum } from './enumeration';

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
    const arr: INodeData[] = [
      {
        nodeRef: 'cron',
        nodeType: NodeTypeEnum.CRON,
        text: 'cron',
      },
      {
        nodeRef: 'webhook',
        nodeType: NodeTypeEnum.WEBHOOK,
        text: 'webhook',
      },
    ];

    return keyword ? arr.filter(({ text }) => text.includes(keyword)) : arr;
  }

  private loadInnerNodes(keyword?: string): INodeData[] {
    const arr: INodeData[] = [
      {
        nodeRef: 'shell',
        nodeType: NodeTypeEnum.SHELL,
        text: 'shell',
      },
    ];

    return keyword ? arr.filter(({ text }) => text.includes(keyword)) : arr;

  }

  private loadLocalNodes(keyword?: string): INodeData[] {
    // TODO 加载本地节点
    return [];
  }

  private loadOfficialNodes(keyword?: string): INodeData[] {
    // TODO 加载hub官方节点
    return [
      {
        nodeRef: 'git_clone',
        nodeType: NodeTypeEnum.ASYNC_TASK,
        image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FikR5g_gILRZjr-olpMqypjhfuj3',
        text: '克隆建木CI代码',
      },
      {
        nodeRef: 'node_build',
        nodeType: NodeTypeEnum.ASYNC_TASK,
        image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FpON0edVLhS5j3Kgvs9i-rwljruu',
        text: 'NodeJs构建前端项目',
      },
      {
        nodeRef: 'docker_build',
        nodeType: NodeTypeEnum.ASYNC_TASK,
        image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FvWtndEdOK9WmEc8WCmvKLYpy2Xv',
        text: 'docker镜像构建',
      },
      {
        nodeRef: 'npm_publish',
        nodeType: NodeTypeEnum.ASYNC_TASK,
        image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FtRbpLVb0vl5qURYdyxMAHE8c7tT',
        text: '发布npm依赖包',
      },
      {
        nodeRef: 'org_gov',
        nodeType: NodeTypeEnum.ASYNC_TASK,
        image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FlENvzR04GwGJMgUvC_UGadygwXl',
        text: '组织治理',
      },
    ];
  }

  private loadCommunityNodes(keyword?: string): INodeData[] {
    // TODO 加载hub社区节点
    return [];

  }
}