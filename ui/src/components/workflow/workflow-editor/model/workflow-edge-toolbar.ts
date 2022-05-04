import { Cell, CellView, Edge, Graph, JQuery } from '@antv/x6';
// @ts-ignore
import listen from 'good-listener';
import edgeRemoveIcon from '../svgs/edge-remove.svg';
import edgeRemoveHoverIcon from '../svgs/edge-remove-hover.svg';
import { EDGE } from '../shape/gengral-config';

const { stroke: lineColor } = EDGE;
const EDGE_REMOVE_ICON_CLASS = 'jm-workflow-editor-edge-remove-icon';

export class WorkflowEdgeToolbar {
  private readonly graph: Graph;
  private edge?: Edge;
  private listener?: any;

  constructor(graph: Graph) {
    this.graph = graph;
  }

  show(edge: Edge): void {
    // 重置
    this.reset(edge);
    // 设置颜色
    this.changeEdgeColor(lineColor.hover);
    // 添加所有工具
    this.addTools();
  }

  hide(): void {
    // 设置颜色
    this.changeEdgeColor(lineColor._default);
    // 移除所有工具
    this.removeTools();
    // 清除
    this.clear();
  }

  private reset(edge: Edge): void {
    this.edge = edge;
    this.listener = listen(this.graph.container, 'mousemove', (e: MouseEvent) => {
      if (!e.target) {
        return;
      }

      const el = e.target as SVGElement;
      const className = el.getAttribute('class');

      if (className !== EDGE_REMOVE_ICON_CLASS) {
        const list = this.graph.container.querySelectorAll<SVGElement>(`.${EDGE_REMOVE_ICON_CLASS}`);
        if (list.length === 0) {
          return;
        }
        list.item(0).setAttribute('xlink:href', edgeRemoveIcon);
        return;
      }

      if (edgeRemoveHoverIcon === el.getAttribute('xlink:href')) {
        return;
      }
      el.setAttribute('xlink:href', edgeRemoveHoverIcon);
    });
  }

  private clear(): void {
    if (this.listener) {
      this.listener.destroy();
    }

    delete this.edge;
    delete this.listener;
  }

  private changeEdgeColor(color: string): void {
    if (!this.edge) {
      return;
    }

    this.edge.setAttrByPath('line/stroke', color);
  }

  private addTools(): void {
    if (!this.edge) {
      return;
    }

    this.edge.addTools([
      {
        // 路径点
        name: 'vertices',
        args: {
          // TODO 根据svg内容确定attr
          attrs: {},
        },
      },
      {
        // 线段
        name: 'segments',
        args: {
          // TODO 根据svg内容确定attr
          attrs: {},
        },
      },
      {
        // 删除按钮
        name: 'button',
        args: {
          distance: '50%',
          markup: [
            {
              tagName: 'image',
              className: EDGE_REMOVE_ICON_CLASS,
              attrs: {
                x: -12,
                y: -12,
                width: 24,
                height: 24,
                'xlink:href': edgeRemoveIcon,
                cursor: 'pointer',
              },
            },
          ],
          onClick: ({ cell }: { e: JQuery.MouseDownEvent, cell: Cell, view: CellView }) => {
            this.graph.removeCell(cell);
          },
        },
      },
    ]);
  }

  private removeTools(): void {
    if (!this.edge) {
      return;
    }

    // 移除路径点
    this.edge.removeTool('vertices');
    // 移除线段
    this.edge.removeTool('segments');
    // 移除删除按钮
    this.edge.removeTool('button');
  }
}