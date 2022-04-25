import { Addon, Graph, Node } from '@antv/x6';
import { INodeData } from '../model/data';
import { ports, shapeSize, shapeTextMaxHeight } from '../shape/gengral-config';

const { width, height } = shapeSize;

export default class WorkflowDnd {
  private readonly graph: Graph;
  private readonly dnd: Addon.Dnd;

  constructor(graph: Graph) {
    this.graph = graph;
    this.dnd = new Addon.Dnd({
      target: graph,
      animation: true,
      getDragNode: (sourceNode: Node) => {
        const { width, height } = sourceNode.getSize();
        sourceNode.resize(width, height + shapeTextMaxHeight);

        // 开始拖拽时初始化的节点，直接使用，无需克隆
        return sourceNode;
      },
      getDropNode: (draggingNode: Node) => {
        const { width, height } = draggingNode.getSize();
        draggingNode.resize(width, height - shapeTextMaxHeight);

        // 结束拖拽时，必须克隆拖动的节点，因为拖动的节点和目标节点不在一个画布
        const targetNode = draggingNode.clone();
        setTimeout(() => {
          // 保证不偏移
          const { x, y } = targetNode.getPosition();
          targetNode.setPosition(x, y - shapeTextMaxHeight / 2);
        });

        return targetNode;
      },
    });
  }

  drag(data: INodeData, event: MouseEvent) {
    const node = this.graph.createNode({
      shape: 'vue-shape',
      width,
      height,
      component: 'custom-vue-shape',
      data: {
        ...data,
      },
      ports: { ...ports },
    });

    this.dnd.start(node, event);
  }
}