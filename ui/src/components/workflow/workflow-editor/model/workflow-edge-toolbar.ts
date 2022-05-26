import { Cell, CellView, Edge, Graph, JQuery } from '@antv/x6';
// @ts-ignore
import listen from 'good-listener';
import edgeRemoveIcon from '../svgs/edge-remove.svg';
import edgeRemoveHoverIcon from '../svgs/edge-remove-hover.svg';
import { EDGE, PORT } from '../shape/gengral-config';

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
          attrs: {
            r: 3,
            'stroke-width': 0,
            fill: '#666666',
          },
        },
      },
      {
        // 线段
        name: 'segments',
        args: {
          attrs: {
            width: 8,
            height: 4,
            x: -4,
            y: -2,
            rx: 1,
            ry: 1,
            'stroke-width': 0,
            fill: '#666666',
          },
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
      {
        // source箭头
        name: 'source-arrowhead',
        args: {
          tagName: 'circle',
          attrs: {
            stroke: lineColor.hover,
            'stroke-width': 1,
            r: PORT.r,
            fill: lineColor.hover,
          },
        },
      },
      {
        // target箭头
        name: 'target-arrowhead',
        args: {
          attrs: {
            d: 'M -11.5 -7 2 0 -11.5 7 Z',
            'stroke-width': 0,
            fill: lineColor.hover,
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
    // 移除source箭头
    this.edge.removeTool('source-arrowhead');
    // 移除target箭头
    this.edge.removeTool('target-arrowhead');
  }
}