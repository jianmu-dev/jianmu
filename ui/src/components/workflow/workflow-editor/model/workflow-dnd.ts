import { Addon, Cell, CellView, Graph, JQuery, Node } from '@antv/x6';
import { INodeData } from '../model/data';
import { PORTS, SHAPE_SIZE, SHAPE_TEXT_MAX_HEIGHT } from '../shape/gengral-config';
import alertImg from '../svgs/alert.svg';

const { width, height } = SHAPE_SIZE;

export default class WorkflowDnd {
  private readonly graph: Graph;
  private readonly dnd: Addon.Dnd;

  constructor(graph: Graph, alertCallback: (data: INodeData) => void) {
    this.graph = graph;
    this.dnd = new Addon.Dnd({
      target: graph,
      animation: true,
      getDragNode: (sourceNode: Node) => {
        const { width, height } = sourceNode.getSize();
        sourceNode.resize(width, height + SHAPE_TEXT_MAX_HEIGHT);

        // 开始拖拽时初始化的节点，直接使用，无需克隆
        return sourceNode;
      },
      getDropNode: (draggingNode: Node) => {
        const { width, height } = draggingNode.getSize();
        draggingNode.resize(width, height - SHAPE_TEXT_MAX_HEIGHT);

        // 结束拖拽时，必须克隆拖动的节点，因为拖动的节点和目标节点不在一个画布
        const targetNode = draggingNode.clone();
        setTimeout(() => {
          // 保证不偏移
          const { x, y } = targetNode.getPosition();
          targetNode.setPosition(x, y - SHAPE_TEXT_MAX_HEIGHT / 2);
        });

        targetNode.addTools({
          name: 'button',
          args: {
            markup: [
              {
                tagName: 'image',
                attrs: {
                  width: 24,
                  height: 24,
                  'xlink:href': alertImg,
                  cursor: 'pointer',
                },
              },
            ],
            x: '100%',
            y: 0,
            offset: { x: -16, y: 0 },
            onClick: ({ cell: { data } }: { e: JQuery.MouseDownEvent, cell: Cell, view: CellView }) => {
              alertCallback(data);
            },
          },
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
      ports: { ...PORTS },
    });

    this.dnd.start(node, event);
  }
}