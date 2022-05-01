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
        ref: 'cron',
        type: NodeTypeEnum.CRON,
        name: 'cron',
        inputs: [],
        outputs: [],
      },
      {
        ref: 'webhook',
        type: NodeTypeEnum.WEBHOOK,
        name: 'webhook',
        inputs: [],
        outputs: [],
      },
    ];

    return keyword ? arr.filter(({ text }) => text.includes(keyword)) : arr;
  }

  private loadInnerNodes(keyword?: string): INodeData[] {
    const arr: INodeData[] = [
      {
        ref: 'shell',
        type: NodeTypeEnum.SHELL,
        name: 'shell',
        inputs: [],
        outputs: [],
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
        ref: 'git_clone',
        type: NodeTypeEnum.ASYNC_TASK,
        icon: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FikR5g_gILRZjr-olpMqypjhfuj3',
        name: '克隆建木CI代码',
        inputs: [],
        outputs: [],
      },
      {
        ref: 'node_build',
        type: NodeTypeEnum.ASYNC_TASK,
        icon: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FpON0edVLhS5j3Kgvs9i-rwljruu',
        name: 'NodeJs构建前端项目',
        inputs: [],
        outputs: [],
      },
      {
        ref: 'docker_build',
        type: NodeTypeEnum.ASYNC_TASK,
        icon: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FvWtndEdOK9WmEc8WCmvKLYpy2Xv',
        name: 'docker镜像构建',
        inputs: [],
        outputs: [],
      },
      {
        ref: 'npm_publish',
        type: NodeTypeEnum.ASYNC_TASK,
        icon: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FtRbpLVb0vl5qURYdyxMAHE8c7tT',
        name: '发布npm依赖包',
        inputs: [],
        outputs: [],
      },
      {
        ref: 'org_gov',
        type: NodeTypeEnum.ASYNC_TASK,
        icon: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FlENvzR04GwGJMgUvC_UGadygwXl',
        name: '组织治理',
        inputs: [],
        outputs: [],
      },
    ];
  }

  private loadCommunityNodes(keyword?: string): INodeData[] {
    // TODO 加载hub社区节点
    return [];

  }
}