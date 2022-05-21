import { Addon, Cell, CellView, Graph, JQuery, Node, Point } from '@antv/x6';
// @ts-ignore
import listen from 'good-listener';
import { IWorkflowNode } from './data/common';
import { NODE, PORTS } from '../shape/gengral-config';
import nodeWarningIcon from '../svgs/node-warning.svg';
import { WorkflowValidator } from './workflow-validator';
import { CustomX6NodeProxy } from './data/custom-x6-node-proxy';

const { icon: { width, height }, textMaxHeight } = NODE;

interface IDraggingListener {
  mousePosition: Point.PointLike;
  listener?: any;
}

export class WorkflowDnd {
  private readonly graph: Graph;
  private readonly dnd: Addon.Dnd;
  private readonly draggingListener: IDraggingListener = {
    mousePosition: { x: -1, y: -1 },
  }

  constructor(graph: Graph,
    workflowValidator: WorkflowValidator,
    nodeContainer: HTMLElement,
    clickNodeWarningCallback: (nodeId: string) => void) {
    this.graph = graph;
    this.dnd = new Addon.Dnd({
      target: graph,
      animation: true,
      getDragNode: (sourceNode: Node) => {
        const { width, height } = sourceNode.getSize();
        sourceNode.resize(width, height + textMaxHeight);

        // 开始拖拽时初始化的节点，直接使用，无需克隆
        return sourceNode;
      },
      getDropNode: (draggingNode: Node) => {
        const { width, height } = draggingNode.getSize();
        draggingNode.resize(width, height - textMaxHeight);

        // 结束拖拽时，必须克隆拖动的节点，因为拖动的节点和目标节点不在一个画布
        const targetNode = draggingNode.clone();
        // 保证不偏移
        setTimeout(() => {
          const { x, y } = targetNode.getPosition();
          targetNode.setPosition(x, y - textMaxHeight / 2);
        });

        const proxy = new CustomX6NodeProxy(targetNode);

        proxy.getData().validate().catch(() => {
          // 校验节点有误时，加警告
          targetNode.addTools({
            name: 'button',
            args: {
              markup: [
                {
                  tagName: 'image',
                  attrs: {
                    width: 24,
                    height: 24,
                    'xlink:href': nodeWarningIcon,
                    cursor: 'pointer',
                  },
                },
              ],
              x: '100%',
              y: 0,
              offset: { x: -13, y: -11 },
              onClick: ({ cell: { id } }: { e: JQuery.MouseDownEvent, cell: Cell, view: CellView }) => {
                clickNodeWarningCallback(id);
              },
            },
          });
        });

        return targetNode;
      },
      validateNode: (droppingNode: Node) => {
        const { mousePosition } = this.draggingListener;
        // 销毁监听器，必须先获取鼠标位置后销毁
        this.destroyListener();

        const nodePanelRect = nodeContainer.getBoundingClientRect();

        return workflowValidator.checkDroppingNode(droppingNode, mousePosition, nodePanelRect);
      },
    });
  }

  drag(data: IWorkflowNode, event: MouseEvent) {
    // 构建监听器
    this.buildListener(event);

    const node = this.graph.createNode({
      shape: 'vue-shape',
      width,
      height,
      component: 'custom-vue-shape',
      ports: { ...PORTS },
    });
    const proxy = new CustomX6NodeProxy(node);
    proxy.setData(data);

    this.dnd.start(node, event);
  }

  private buildListener({ x, y }: MouseEvent) {
    this.draggingListener.mousePosition = { x, y };
    this.draggingListener.listener = listen(document.body, 'mousemove', (e: MouseEvent) => {
      this.draggingListener.mousePosition.x = e.x;
      this.draggingListener.mousePosition.y = e.y;
    });
  }

  private destroyListener() {
    if (this.draggingListener.listener) {
      this.draggingListener.listener.destroy();
    }

    this.draggingListener.mousePosition = { x: -1, y: -1 };
    delete this.draggingListener.listener;
  }
}