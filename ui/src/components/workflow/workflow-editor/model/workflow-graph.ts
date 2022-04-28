import { Graph, Shape } from '@antv/x6';
import normalizeWheel from 'normalize-wheel';
import { nextTick } from 'vue';

interface IGraphPosition {
  x: number;
  y: number;
}

// 容器大小比例，相对父元素
const CONTAINER_SIZE_SCALE = 2;

export default class WorkflowGraph {
  private readonly container: HTMLElement;
  private readonly graph: Graph;
  private readonly containerPosition: IGraphPosition = { x: 0, y: 0 };

  constructor(container: HTMLElement) {
    this.container = container;

    // #region 初始化画布
    this.graph = new Graph({
      container,
      grid: true,
      history: true,
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

    // 保证渲染完成
    nextTick(() => {
      // 初始化大小&坐标
      this.initContainer();

      // 注册容器大小变化监听器
      this.registerContainerResizeListener();
    }).then(() => {
    });
  }

  /**
   * 滚轮容器
   * @param e
   */
  wheelScrollContainer(e: WheelEvent) {
    const containerParent = this.container.parentElement!;

    const minX = containerParent.clientWidth - this.container.offsetWidth;
    const minY = containerParent.clientHeight - this.container.offsetHeight;
    const maxX = 0;
    const maxY = 0;

    // 画布滚动事件
    const { pixelX, pixelY } = normalizeWheel(e);

    let tempX = this.containerPosition.x - pixelX;
    let tempY = this.containerPosition.y - pixelY;

    if (tempX > maxX) {
      tempX = maxX;
    } else if (tempX < minX) {
      tempX = minX;
    }

    if (tempY > maxY) {
      tempY = maxY;
    } else if (tempY < minY) {
      tempY = minY;
    }

    this.moveContainer(tempX, tempY);
  }

  /**
   * 初始化容器
   * @private
   */
  private initContainer() {
    this.resizeContainer();

    const containerParent = this.container.parentElement!;

    // 水平&垂直居中
    const x = (containerParent.clientWidth - this.container.offsetWidth) / 2;
    const y = (containerParent.clientHeight - this.container.offsetHeight) / 2;

    this.moveContainer(x, y);
  }

  /**
   * 移动容器
   * @param x
   * @param y
   * @private
   */
  private moveContainer(x: number, y: number) {
    this.containerPosition.x = x;
    this.containerPosition.y = y;

    this.container.style.left = `${x}px`;
    this.container.style.top = `${y}px`;
  }

  /**
   * 改变容器大小
   * @private
   */
  private resizeContainer() {
    const containerParent = this.container.parentElement!;

    const w = containerParent.clientWidth * CONTAINER_SIZE_SCALE;
    const h = containerParent.clientHeight * CONTAINER_SIZE_SCALE;

    this.container.style.width = `${w}px`;
    this.container.style.height = `${h}px`;
  }

  /**
   * 注册容器大小变化监听器
   * @private
   */
  private registerContainerResizeListener() {
    const containerParent = this.container.parentElement!;

    new ResizeObserver(() => this.resizeContainer()).observe(containerParent);
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

    // undo redo
    this.graph.bindKey(['meta+z', 'ctrl+z'], () => {
      if (this.graph.history.canUndo()) {
        this.graph.history.undo();
      }
      return false;
    });
    this.graph.bindKey(['meta+shift+z', 'ctrl+shift+z'], () => {
      if (this.graph.history.canRedo()) {
        this.graph.history.redo();
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

    // zoom
    this.graph.bindKey(['ctrl+1', 'meta+1'], () => {
      const zoom = this.graph.zoom();
      if (zoom < 1.5) {
        this.graph.zoom(0.1);
      }
    });
    this.graph.bindKey(['ctrl+2', 'meta+2'], () => {
      const zoom = this.graph.zoom();
      if (zoom > 0.5) {
        this.graph.zoom(-0.1);
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