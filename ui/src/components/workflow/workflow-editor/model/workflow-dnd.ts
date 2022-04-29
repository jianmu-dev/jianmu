import { Addon, Cell, CellView, Graph, JQuery, Node } from '@antv/x6';
// @ts-ignore
import listen from 'good-listener';
import { INodeData } from '../model/data';
import { PORTS, SHAPE_SIZE, SHAPE_TEXT_MAX_HEIGHT } from '../shape/gengral-config';
import alertImg from '../svgs/alert.svg';
import { NodeTypeEnum } from '../model/enumeration';

const { width, height } = SHAPE_SIZE;

interface IDraggingListener {
  mousePosX: number;
  mousePosY: number;
  listener?: any;
}

export default class WorkflowDnd {
  private readonly graph: Graph;
  private readonly dnd: Addon.Dnd;
  private readonly graphPanelEl: HTMLElement;
  private readonly draggingListener: IDraggingListener = {
    mousePosX: -1,
    mousePosY: -1,
  }

  constructor(graph: Graph, graphPanelEl: HTMLElement, alertCallback: (data: INodeData) => void) {
    this.graph = graph;
    this.graphPanelEl = graphPanelEl;
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

        // TODO 未填写必填参数时显示警告
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
      validateNode: (droppingNode: Node) => {
        const { mousePosX, mousePosY } = this.draggingListener;
        const { x, y, width, height } = this.graphPanelEl.getBoundingClientRect();
        const maxX = x + width;
        const maxY = y + height;

        // 销毁监听器
        this.destroyListener();

        const { nodeType } = droppingNode.getData<INodeData>();

        if ([NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(nodeType)) {
          // 表示当前拖放的节点为trigger
          const currentTrigger = this.graph.getNodes().find(node =>
            [NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(node.getData<INodeData>().nodeType));

          if (currentTrigger) {
            // TODO 需加提示
            console.log('只能有一个触发器节点');
            return false;
          }
        }

        if (mousePosX >= x && mousePosX <= maxX &&
          mousePosY >= y && mousePosY <= maxY) {
          // 在画布面板中拖放时，才能成功
          return true;
        }

        return false;
      },
    });
  }

  drag(data: INodeData, event: MouseEvent) {
    // 构建监听器
    this.buildListener();

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

  private buildListener() {
    this.draggingListener.listener = listen(document.body, 'mousemove', (e: MouseEvent) => {
      this.draggingListener.mousePosX = e.x;
      this.draggingListener.mousePosY = e.y;
    });
  }

  private destroyListener() {
    if (this.draggingListener.listener) {
      this.draggingListener.listener.destroy();
    }

    this.draggingListener.mousePosX = -1;
    this.draggingListener.mousePosY = -1;
    delete this.draggingListener.listener;
  }
}