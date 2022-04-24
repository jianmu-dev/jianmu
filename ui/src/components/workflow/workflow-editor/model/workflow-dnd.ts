import { Addon, Graph, Node } from '@antv/x6';
import { INodeData } from '../model/data';
import { ports } from '../shape/gengral-config';

export default class WorkflowDnd {
  private readonly graph: Graph;
  private readonly dnd: Addon.Dnd;

  constructor(graph: Graph) {
    this.graph = graph;
    this.dnd = new Addon.Dnd({
      target: graph,
      animation: true,
      getDragNode: (sourceNode: Node) => {
        // 开始拖拽时初始化的节点，直接使用，无需克隆
        return sourceNode;
      },
      // getDropNode: ({ data }: Node) => {
      //   return this.graph.createNode({
      //     shape: 'vue-shape',
      //     width: 80,
      //     height: 80,
      //     component: 'custom-vue-shape',
      //     data: {
      //       ...data,
      //     },
      //     ports: { ...ports },
      //   });
      // },
    });
  }

  drag(data: INodeData, event: MouseEvent) {
    const node = this.graph.createNode({
      shape: 'vue-shape',
      width: 80,
      height: 80,
      component: 'custom-vue-shape',
      data: {
        ...data,
      },
      ports: { ...ports },
    });

    this.dnd.start(node, event);
  }
}