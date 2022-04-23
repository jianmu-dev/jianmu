import { Addon, Graph } from '@antv/x6';
import { INodeData } from './data';
import cron from '../shapes/trigger/cron';
import webhook from '../shapes/trigger/webhook';
import shell from '../shapes/inner/shell';
import { ports, shapeSize } from '../shapes/gengral-config';
import { NodeTypeEnum } from '../model/enumeration';

export default class WorkflowStencil {
  private readonly graph: Graph;
  private readonly stencil: Addon.Stencil;

  constructor(graph: Graph, stencilContainer: HTMLElement) {
    this.graph = graph;
    this.stencil = new Addon.Stencil({
      title: '流程图',
      target: graph,
      stencilGraphWidth: 200,
      stencilGraphHeight: 180,
      collapsable: false,
      groups: [
        {
          title: '触发器',
          name: 'inner_triggers',
        },
        {
          title: '内置节点',
          name: 'inner_nodes',
        },
        // {
        //   title: '本地节点',
        //   name: 'local_nodes',
        // },
        {
          title: '官网节点',
          name: 'official_nodes',
        },
        // {
        //   title: '社区节点',
        //   name: 'community_nodes',
        // },
      ],
      layoutOptions: {
        columns: 2,
        columnWidth: 80,
        rowHeight: 55,
      },
    });
    stencilContainer.appendChild(this.stencil.container);

    this.loadInnerTriggers();
    this.loadInnerNodes();
    // TODO 异步获取
    this.loadLocalNodes([]);
    this.loadOfficialNodes([]);
    this.loadCommunityNodes([]);
  }

  private loadInnerTriggers() {
    this.stencil.load([cron, webhook], 'inner_triggers');
  }

  private loadInnerNodes() {
    this.stencil.load([shell], 'inner_nodes');
  }

  loadLocalNodes(nodes: INodeData[]) {

  }

  loadOfficialNodes(nodes: INodeData[]) {
    const arr = [{
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FikR5g_gILRZjr-olpMqypjhfuj3',
      text: '克隆建木CI代码',
    }, {
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FpON0edVLhS5j3Kgvs9i-rwljruu',
      text: 'NodeJs构建前端项目',
    }];

    const { width, height } = shapeSize;

    this.stencil.load(arr.map(({ image, text }) => ({
      shape: 'vue-shape',
      width,
      height,
      component: 'custom-vue-shape',
      data: {
        nodeType: NodeTypeEnum.ASYNC_TASK,
        image,
        text,
      },
      ports: { ...ports },
    })), 'official_nodes');
  }

  loadCommunityNodes(nodes: INodeData[]) {

  }

  get x6AddonStencil(): Addon.Stencil {
    return this.stencil;
  }
}