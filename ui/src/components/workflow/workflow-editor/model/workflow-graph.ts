import { Cell, Graph, Shape } from '@antv/x6';
import normalizeWheel from 'normalize-wheel';
import { WorkflowTool } from './workflow-tool';
import { ZoomTypeEnum } from './data/enumeration';
import { WorkflowNodeToolbar } from './workflow-node-toolbar';
import { EDGE, PORT } from '../shape/gengral-config';
import { WorkflowEdgeToolbar } from './workflow-edge-toolbar';

const { stroke: lineColor } = EDGE;
const { fill: circleBgColor } = PORT;

export default class WorkflowGraph {
  private readonly graph: Graph;
  private readonly clickNodeCallback: (nodeId: string) => void;
  readonly workflowNodeToolbar: WorkflowNodeToolbar;
  private readonly workflowEdgeToolbar: WorkflowEdgeToolbar;

  constructor(proxy: any, container: HTMLElement, clickNodeCallback: (nodeId: string) => void) {
    const containerParentEl = container.parentElement!.parentElement!;
    this.clickNodeCallback = clickNodeCallback;

    // #region 初始化画布
    this.graph = new Graph({
      container,
      width: containerParentEl.clientWidth,
      height: containerParentEl.clientHeight,
      // 不绘制网格背景
      grid: false,
      connecting: {
        anchor: 'center',
        connectionPoint: 'anchor',
        // 禁止在相同的起始节点和终止之间创建多条边
        allowMulti: false,
        allowBlank: ({ sourcePort }) => {
          const sourceMagnet = Array.from(this.graph.container.querySelectorAll<SVGElement>('.x6-port-body'))
            .find(port => sourcePort === port.getAttribute('port'))!;
          sourceMagnet.setAttribute('fill', circleBgColor._default);

          // 禁止连接到画布空白位置的点
          return false;
        },
        // 禁止创建循环连线，即边的起始节点和终止节点为同一节点
        allowLoop: false,
        // 禁止边链接到节点（非节点上的链接桩）
        allowNode: false,
        // 禁止边链接到另一个边
        allowEdge: false,
        snap: {
          radius: 20,
        },
        createEdge() {
          return new Shape.Edge({
            attrs: {
              line: {
                // 虚线
                strokeDasharray: '4,1.5',
                stroke: lineColor.connecting,
                strokeWidth: 1.5,
                targetMarker: {
                  name: 'block',
                  width: 12,
                  height: 12,
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
          // 移除虚线
          edge.removeAttrByPath('line/strokeDasharray');
          // 设置颜色
          edge.setAttrByPath('line/stroke', lineColor._default);
          return true;
        },
        validateConnection({ targetMagnet }) {
          return !!targetMagnet;
        },
        validateMagnet({ e, magnet, view, cell }) {
          magnet.setAttribute('fill', circleBgColor.connectingSource);
          return true;
        },
      },
      highlighting: {
        // 连线过程中，自动吸附到链接桩时被使用
        magnetAdsorbed: {
          name: 'stroke',
          args: {
            padding: 0,
            attrs: {
              stroke: circleBgColor.connectingTarget,
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

    // 初始化节点工具栏
    this.workflowNodeToolbar = new WorkflowNodeToolbar(proxy, this.graph);
    // 初始化边工具栏
    this.workflowEdgeToolbar = new WorkflowEdgeToolbar(this.graph);

    // 不激活快捷键
    // this.registerShortcut();
    this.bindEvent();

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

      const workflowTool = new WorkflowTool(this.graph);
      // 渲染完成后，适屏展示
      workflowTool.zoom(ZoomTypeEnum.FIT);
      if (this.graph.zoom() > 1) {
        // 适屏后，缩放比例超过100%，原始大小展示
        workflowTool.zoom(ZoomTypeEnum.ORIGINAL);
      }
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

    // 隐藏节点工具栏
    this.workflowNodeToolbar.hide();
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
   * @private
   */
  private bindEvent() {
    this.graph.on('node:mousedown', ({ e }) => {
      // 隐藏节点工具栏
      this.workflowNodeToolbar.hide(e);
    });
    this.graph.on('node:mouseup', ({ node }) => {
      // 显示节点工具栏
      this.workflowNodeToolbar.show(node);
    });
    this.graph.on('node:click', ({ e, node }) => {
      if (e.target.getAttribute('class') === 'x6-port-body') {
        // 表示点击连接桩，忽略
        return;
      }

      this.clickNodeCallback(node.id);
    });
    this.graph.on('node:mouseenter', ({ node }) => {
      // 显示节点工具栏
      this.workflowNodeToolbar.show(node);
    });
    this.graph.on('node:mouseleave', ({ e }) => {
      // 隐藏节点工具栏
      this.workflowNodeToolbar.hide(e.originalEvent);
    });

    this.graph.on('edge:mouseenter', ({ edge }) => {
      // 显示边工具栏
      this.workflowEdgeToolbar.show(edge);
    });
    this.graph.on('edge:mouseleave', () => {
      // 隐藏边工具栏
      this.workflowEdgeToolbar.hide();
    });
  }

  /**
   * 获取x6 graph对象
   */
  get x6Graph(): Graph {
    return this.graph;
  }
}