import { Addon, Graph } from '@antv/x6';
import { INodeData } from '@/components/workflow/workflow-editor/layout/model/data';

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
    const r1 = this.graph.createNode({
      shape: 'custom-rect',
      label: 'Cron',
      attrs: {
        body: {
          rx: 20,
          ry: 26,
        },
      },
    });
    const r2 = this.graph.createNode({
      shape: 'custom-rect',
      label: 'Webhook',
    });
    this.stencil.load([r1, r2], 'inner_triggers');
  }

  private loadInnerNodes() {
    const r3 = this.graph.createNode({
      shape: 'custom-rect',
      attrs: {
        body: {
          rx: 6,
          ry: 6,
        },
      },
      label: 'Shell',
      data: {
        type: 'shell',
      },
    });
    this.stencil.load([r3], 'inner_nodes');
  }

  loadLocalNodes(nodes: INodeData[]) {

  }

  loadOfficialNodes(nodes: INodeData[]) {
    const r1 = this.graph.createNode({
      shape: 'custom-image',
      label: 'git loggit loggit loggit loggit loggit loggit loggit log',
      attrs: {
        image: {
          'xlink:href': 'https://jianmuhub.img.dghub.cn/node-definition/icon/FikR5g_gILRZjr-olpMqypjhfuj3?roundPic/radius/!25.5p',
        },
      },
    });

    this.stencil.load([r1], 'official_nodes');
  }

  loadCommunityNodes(nodes: INodeData[]) {

  }

  get x6AddonStencil(): Addon.Stencil {
    return this.stencil;
  }
}