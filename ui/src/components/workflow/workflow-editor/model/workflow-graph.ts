import { Cell, Graph, Shape } from '@antv/x6';
import normalizeWheel from 'normalize-wheel';
import { IWorkflowNode } from './data/common';
import { CustomX6NodeProxy } from './data/custom-x6-node-proxy';

export default class WorkflowGraph {
  private readonly graph: Graph;
  private readonly clickNodeCallback: (data: IWorkflowNode) => void;

  constructor(container: HTMLElement, clickNodeCallback: (data: IWorkflowNode) => void) {
    const containerParentEl = container.parentElement!;
    this.clickNodeCallback = clickNodeCallback;

    // #region 初始化画布
    this.graph = new Graph({
      container,
      width: containerParentEl.clientWidth,
      height: containerParentEl.clientHeight,
      // 不绘制网格背景
      grid: false,
      mousewheel: {
        enabled: true,
        zoomAtMousePosition: true,
        modifiers: 'ctrl',
        minScale: 0.5,
        maxScale: 3,
      },
      connecting: {
        router: {
          name: 'manhattan',
          args: {
            padding: 1,
          },
        },
        connector: {
          name: 'rounded',
          args: {
            radius: 8,
          },
        },
        anchor: 'center',
        connectionPoint: 'anchor',
        allowBlank: false,
        snap: {
          radius: 20,
        },
        createEdge() {
          return new Shape.Edge({
            attrs: {
              line: {
                // 虚线
                strokeDasharray: '4,2,1,2',
                stroke: '#A2B1C3',
                strokeWidth: 2,
                targetMarker: {
                  name: 'block',
                  width: 12,
                  height: 8,
                },
              },
            },
            router: {
              // 直线路由
              name: 'normal',
            },
            connector: {
              // 圆角连接器
              name: 'rounded',
            },
            zIndex: 0,
          });
        },
        validateEdge({ edge, type, previous }) {
          // this.graph.batchUpdate('add-edge', () => {
          // TODO UNDO/REDO有问题
          // 移除虚线
          edge.removeAttrByPath('line/strokeDasharray');
          // });
          return true;
        },
        validateConnection({ targetMagnet }) {
          return !!targetMagnet;
        },
      },
      highlighting: {
        magnetAdsorbed: {
          name: 'stroke',
          args: {
            attrs: {
              fill: '#5F95FF',
              stroke: '#5F95FF',
            },
          },
        },
      },
      resizing: false,
      rotating: false,
      selecting: {
        enabled: true,
        // 是否框选
        rubberband: true,
        showNodeSelectionBox: true,
      },
      snapline: true,
      keyboard: true,
      clipboard: true,
    });

    this.registerShortcut();
    this.bindEvent(container);

    // 注册容器大小变化监听器
    this.registerContainerResizeListener(containerParentEl);
  }

  render(data: string) {
    if (!data) {
      return;
    }

    // 启用异步渲染的画布
    // 异步渲染不会阻塞 UI，对需要添加大量节点和边时的性能提升非常明显
    this.graph.setAsync(true);
    // 注册渲染事件
    this.graph.on('render:done', () => {
      // 确保所有变更都已经生效，然后在事件回调中进行这些操作。

      // 初始化完成后
      // 1. 禁用异步渲染的画布。
      this.graph.setAsync(false);
      // 2. 注销渲染事件
      this.graph.off('render:done');
    });

    // 启用异步渲染的画布处于冻结状态
    // 处于冻结状态的画布不会立即响应画布中节点和边的变更，直到调用 unfreeze(...) 方法来解除冻结并重新渲染画布
    this.graph.freeze();

    const propertiesArr: Cell.Properties[] = JSON.parse(data).cells;
    this.graph.resetCells(propertiesArr.map(properties => {
      if (properties.shape === 'edge') {
        return this.graph.createEdge(properties);
      }
      return this.graph.createNode(properties);
    }));

    // 解除冻结并重新渲染画布
    this.graph.unfreeze();
  }

  /**
   * 滚轮容器
   * @param e
   */
  wheelScrollContainer(e: WheelEvent) {
    // 画布滚动事件
    const { pixelX, pixelY } = normalizeWheel(e);

    this.graph.translateBy(-pixelX, -pixelY);
  }

  /**
   * 注册容器大小变化监听器
   * @private
   */
  private registerContainerResizeListener(containerParentEl: HTMLElement) {
    new ResizeObserver(() => {
      const { clientWidth, clientHeight } = containerParentEl;
      this.graph.resizeGraph(clientWidth, clientHeight);
    }).observe(containerParentEl);
  }

  /**
   * 注册快捷键
   * @private
   */
  private registerShortcut() {
    // copy cut paste
    this.graph.bindKey(['meta+c', 'ctrl+c'], () => {
      const cells = this.graph.getSelectedCells();
      if (cells.length) {
        this.graph.copy(cells);
      }
      return false;
    });
    this.graph.bindKey(['meta+x', 'ctrl+x'], () => {
      const cells = this.graph.getSelectedCells();
      if (cells.length) {
        this.graph.cut(cells);
      }
      return false;
    });
    this.graph.bindKey(['meta+v', 'ctrl+v'], () => {
      if (!this.graph.isClipboardEmpty()) {
        const cells = this.graph.paste({ offset: 32 });
        this.graph.cleanSelection();
        this.graph.select(cells);
      }
      return false;
    });

    // select all
    this.graph.bindKey(['meta+a', 'ctrl+a'], () => {
      const nodes = this.graph.getNodes();
      if (nodes) {
        this.graph.select(nodes);
      }
    });

    // delete
    this.graph.bindKey('backspace', () => {
      const cells = this.graph.getSelectedCells();
      if (cells.length) {
        this.graph.removeCells(cells);
      }
    });
  }

  /**
   * 绑定事件
   * @param container
   * @private
   */
  private bindEvent(container: HTMLElement) {
    // 控制连接桩显示/隐藏
    const showPorts = (ports: NodeListOf<SVGElement>, show: boolean) => {
      for (let i = 0, len = ports.length; i < len; i = i + 1) {
        ports[i].style.visibility = show ? 'visible' : 'hidden';
      }
    };

    this.graph.on('node:click', ({ node }) => {
      const proxy = new CustomX6NodeProxy(node);
      this.clickNodeCallback(proxy.getData());
    });

    this.graph.on('node:mouseenter', ({ node }) => {
      const ports = container.querySelectorAll(
        '.x6-port-body',
      ) as NodeListOf<SVGElement>;
      showPorts(ports, true);

      // 添加删除按钮
      node.addTools({
        name: 'button-remove',
        args: {
          x: 0,
          y: 0,
          // TODO 根据svg内容确定markup
          markup: {},
        },
      });
    });
    this.graph.on('node:mouseleave', ({ node }) => {
      const ports = container.querySelectorAll(
        '.x6-port-body',
      ) as NodeListOf<SVGElement>;
      showPorts(ports, false);

      // 移除删除按钮
      node.removeTool('button-remove');
    });

    this.graph.on('edge:mouseenter', ({ cell }) => {
      // 添加所有工具
      cell.addTools([
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
          name: 'button-remove',
          args: {
            distance: '50%',
            // TODO 根据svg内容确定markup
            markup: {},
          },
        },
      ]);
    });
    this.graph.on('edge:mouseleave', ({ cell }) => {
      // 移除路径点
      cell.removeTool('vertices');
      // 移除线段
      cell.removeTool('segments');
      // 移除删除按钮
      cell.removeTool('button-remove');
    });
  }

  /**
   * 获取x6 graph对象
   */
  get x6Graph(): Graph {
    return this.graph;
  }
}