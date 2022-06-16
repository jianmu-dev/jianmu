import { Cell, Edge, Graph, Node, Shape } from '@antv/x6';
import normalizeWheel from 'normalize-wheel';
import { WorkflowTool } from './workflow-tool';
import { ZoomTypeEnum } from './data/enumeration';
import { showPort, showPorts, WorkflowNodeToolbar } from './workflow-node-toolbar';
import { EDGE, NODE, PORT, PORTS } from '../shape/gengral-config';
import { WorkflowEdgeToolbar } from './workflow-edge-toolbar';
import { CustomX6NodeProxy } from './data/custom-x6-node-proxy';

const { icon: { width, height } } = NODE;
const { stroke: lineColor } = EDGE;
const { fill: circleBgColor } = PORT;

export function render(graph: Graph, data: string, workflowTool: WorkflowTool) {
  if (!data) {
    return;
  }

  // 启用异步渲染的画布
  // 异步渲染不会阻塞 UI，对需要添加大量节点和边时的性能提升非常明显
  graph.setAsync(true);
  // 注册渲染事件
  graph.on('render:done', () => {
    // 确保所有变更都已经生效，然后在事件回调中进行这些操作。

    // 初始化完成后
    // 1. 禁用异步渲染的画布。
    graph.setAsync(false);
    // 2. 注销渲染事件
    graph.off('render:done');

    // 渲染完成后，居中展示
    workflowTool.zoom(ZoomTypeEnum.CENTER);
  });

  // 启用异步渲染的画布处于冻结状态
  // 处于冻结状态的画布不会立即响应画布中节点和边的变更，直到调用 unfreeze(...) 方法来解除冻结并重新渲染画布
  graph.freeze();

  const propertiesArr: Cell.Properties[] = JSON.parse(data).cells;
  graph.resetCells(propertiesArr.map(properties => {
    if (properties.shape === 'edge') {
      return graph.createEdge(properties);
    }
    return graph.createNode(properties);
  }));

  // 解除冻结并重新渲染画布
  graph.unfreeze();
}

export class WorkflowGraph {
  private readonly graph: Graph;
  private readonly clickNodeCallback: (nodeId: string) => void;
  private readonly workflowTool: WorkflowTool;
  readonly workflowNodeToolbar: WorkflowNodeToolbar;
  private readonly workflowEdgeToolbar: WorkflowEdgeToolbar;
  private readonly resizeObserver: ResizeObserver;

  constructor(proxy: any, container: HTMLElement, clickNodeCallback: (nodeId: string) => void) {
    const containerParentEl = container.parentElement!;
    const { clientWidth: width, clientHeight: height } = containerParentEl;
    this.clickNodeCallback = clickNodeCallback;

    // #region 初始化画布
    this.graph = new Graph({
      container,
      width,
      height,
      // 不绘制网格背景
      grid: false,
      connecting: {
        // 通过高亮+允许多边机制，实现修改边的起点或终点时，确定可连接的连接桩
        highlight: true,
        allowMulti: true,
        anchor: 'center',
        connectionPoint: 'anchor',
        allowBlank: () => {
          // 隐藏所有连接桩
          this.hideAllPorts();

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
                'stroke-width': 1.5,
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
        validateEdge: ({ edge, type, previous }) => {
          // 移除虚线
          edge.removeAttrByPath('line/strokeDasharray');
          // 设置颜色
          edge.setAttrByPath('line/stroke', lineColor._default);

          return true;
        },
        validateConnection: ({
          type, edge: currentEdge, targetMagnet,
          sourceCell, targetCell,
          sourcePort, targetPort,
        }) => {
          const originalSourceNode = currentEdge?.getSourceNode();
          const originalTargetNode = currentEdge?.getTargetNode();
          if (!currentEdge || !originalSourceNode || !originalTargetNode) {
            return !!targetMagnet;
          }

          // 表示修改边
          const sourceNode = sourceCell as Node;
          const targetNode = targetCell as Node;
          const isChangingTarget = type === 'target';

          const originalNode = isChangingTarget ? originalTargetNode : originalSourceNode;
          const currentNode = isChangingTarget ? targetNode : sourceNode;
          // 通过业务校验实现禁止在相同的起始节点和终止之间创建多条边
          if (originalNode === currentNode) {
            showPort(currentNode, (isChangingTarget ? targetPort : sourcePort)!);
          } else {
            if (isChangingTarget) {
              if (new CustomX6NodeProxy(currentNode).isTrigger()) {
                // 触发器节点只能出，不能入
                return !!targetMagnet;
              }
            }

            if (this.getNodesInLine(
              isChangingTarget ? sourceNode : targetNode,
              // 过滤当前在修改的边
              this.graph.getEdges().filter(edge => edge !== currentEdge),
              !isChangingTarget)
              .find(node => node === currentNode)) {
              // 表示当前节点在环路中，不能连
              return !!targetMagnet;
            }

            showPorts(this.graph, currentNode, isChangingTarget);
          }

          return !!targetMagnet;
        },
        validateMagnet: ({ magnet, cell }) => {
          // 隐藏节点工具栏，但显示连接桩
          this.workflowNodeToolbar.hide(magnet.getAttribute('port')!);
          // 显示可连接的连接桩
          this.showConnectablePorts(cell as Node);

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
      resizing: true,
      rotating: false,
      selecting: {
        enabled: true,
        // 是否框选
        rubberband: true,
        // safari存在选中节点时，虚线框的宽度有偏差，用resizing代替
        // fix: #I5CK8M
        showNodeSelectionBox: true,
      },
      snapline: true,
      keyboard: true,
      clipboard: true,
    });

    // 初始化工具
    this.workflowTool = new WorkflowTool(this.graph);
    // 初始化节点工具栏
    this.workflowNodeToolbar = new WorkflowNodeToolbar(proxy, this.graph);
    // 初始化边工具栏
    this.workflowEdgeToolbar = new WorkflowEdgeToolbar(this.graph);

    // 激活快捷键
    this.registerShortcut();
    this.bindEvent();

    // 注册容器大小变化监听器
    this.resizeObserver = new ResizeObserver(() => {
      const { clientWidth, clientHeight } = containerParentEl;
      this.graph.resizeGraph(clientWidth, clientHeight);
    });
    this.resizeObserver.observe(containerParentEl);
  }

  render(data: string) {
    render(this.graph, data, this.workflowTool);
  }

  /**
   * 滚轮滚动
   * @param e
   */
  wheelScroll(e: WheelEvent) {
    // 画布滚动事件
    const { pixelX, pixelY } = normalizeWheel(e);

    this.graph.translateBy(-pixelX, -pixelY);

    // 隐藏节点工具栏
    this.workflowNodeToolbar.hide();
  }

  /**
   * 销毁
   */
  destroy() {
    this.resizeObserver.disconnect();
  }

  /**
   * 注册快捷键
   * @private
   */
  private registerShortcut() {
    // copy cut paste
    this.graph.bindKey(['meta+c', 'ctrl+c'], () => {
      const nodes = this.graph.getSelectedCells()
        // 筛选节点，只能复制节点
        .filter(cell => cell.isNode())
        .map(cell => {
          // 复制/粘贴时，连接桩id没有改变，通过创建新节点刷新连接桩id
          const node = this.graph.createNode({
            shape: 'vue-shape',
            width,
            height,
            component: 'custom-vue-shape',
            ports: { ...PORTS },
            position: (cell as Node).getPosition(),
          });
          const cellProxy = new CustomX6NodeProxy(cell as Node);
          const proxy = new CustomX6NodeProxy(node);
          proxy.setData(cellProxy.getData());
          return node;
        });

      if (nodes.length === 0) {
        return;
      }
      this.graph.copy(nodes);
    });
    // this.graph.bindKey(['meta+x', 'ctrl+x'], () => {
    //   const cells = this.graph.getSelectedCells();
    //   if (cells.length) {
    //     this.graph.cut(cells);
    //   }
    // });
    this.graph.bindKey(['meta+v', 'ctrl+v'], () => {
      if (this.graph.isClipboardEmpty()) {
        return;
      }
      this.graph.paste({ offset: 32 });
      this.graph.cleanSelection();
    });

    // select all
    this.graph.bindKey(['meta+a', 'ctrl+a'], () => {
      const nodes = this.graph.getNodes();
      if (nodes) {
        this.graph.select(nodes);
      }
    });

    // // delete
    // this.graph.bindKey('backspace', () => {
    //   const cells = this.graph.getSelectedCells();
    //   if (cells.length) {
    //     this.graph.removeCells(cells);
    //   }
    // });
  }

  /**
   * 绑定事件
   * @private
   */
  private bindEvent() {
    this.graph.on('node:selected', () => {
      this.workflowTool.optimizeSelectionBoxStyle();
    });
    this.graph.on('node:mousedown', () => {
      // 隐藏节点工具栏
      this.workflowNodeToolbar.hide();
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
   * 显示可连接的连接桩
   * pipeline节点边只能有一个进、一个出
   * @param currentNode
   * @private
   */
  private showConnectablePorts(currentNode: Node): void {
    const allEdges = this.graph.getEdges();
    const excludedNodes = this.getNodesInLine(currentNode, [...allEdges], false);

    this.graph.getNodes()
      // 环路检测：排除以当前节点为终点的上游所有节点
      .filter(node => !excludedNodes.includes(node))
      // 筛选不存在入边的所有节点
      .filter(node => {
        const nodePortIds = node.getPorts().map(metadata => metadata.id);
        return !allEdges.find(edge => {
          const { port: targetPortId } = edge.getTarget() as Edge.TerminalCellData;
          return nodePortIds.includes(targetPortId);
        });
      })
      // 筛选非触发器节点
      .filter(node => !new CustomX6NodeProxy(node).isTrigger())
      .forEach(node =>
        node.getPorts().forEach(port => {
          node.portProp(port.id!, {
            attrs: {
              circle: {
                r: PORT.r,
                // 连接桩在连线交互时可以被连接
                magnet: true,
                fill: circleBgColor._default,
              },
            },
          });
        }));
  }

  /**
   * 隐藏所有连接桩
   * @private
   */
  private hideAllPorts() {
    this.graph.getNodes().forEach(node =>
      node.getPorts().forEach(port =>
        node.portProp(port.id!, {
          attrs: {
            circle: {
              r: 0,
              // 连接桩在连线交互时不可被连接
              magnet: false,
              fill: circleBgColor._default,
            },
          },
        })));
  }

  /**
   * 获取以当前节点为终/起点的上/下游所有节点
   * @param currentNode
   * @param edges
   * @param forward true表示下游；false表示上游
   * @private
   */
  private getNodesInLine(currentNode: Node, edges: Edge[], forward: boolean): Node[] {
    const nodes: Node[] = [currentNode];

    const targetNodePortsIds = currentNode.getPorts().map(metadata => metadata.id);
    const index = edges.findIndex(edge => {
      const { port: targetPortId } = (forward ? edge.getSource() : edge.getTarget()) as Edge.TerminalCellData;

      return targetNodePortsIds.includes(targetPortId);
    });

    if (index >= 0) {
      const edge = edges.splice(index, 1)[0];
      nodes.push(...this.getNodesInLine((forward ? edge.getTargetNode() : edge.getSourceNode())!, edges, forward));
    }

    return nodes;
  }

  /**
   * 获取x6 graph对象
   */
  get x6Graph(): Graph {
    return this.graph;
  }
}