import { G6Event, Graph, IBBox, IG6GraphEvent, Item, LayoutConfig, NodeConfig } from '@antv/g6';
import { parse } from './dsl';
import { NodeTypeEnum } from './enumeration';
import { DslTypeEnum, TaskStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { size } from '@/components/workflow/workflow-viewer/shapes/async-task';
import { INodeMouseoverEvent } from '@/components/workflow/workflow-viewer/utils/model';
import { INodeDefVo } from '@/api/dto/project';
import './array';

/**
 * 检查内容是否溢出
 * @param graph
 * @param ratio
 */
function checkContentOverflow(graph: Graph, ratio: number = 1): boolean {
  const bBoxes: IBBox[] = [];
  bBoxes.push(...graph.getNodes().map(node => node.getCanvasBBox()));
  bBoxes.push(...graph.getEdges().map(node => node.getCanvasBBox()));

  // 初始未溢出
  let tag = false;

  for (const { maxX, maxY } of bBoxes) {
    tag = maxX > graph.getWidth() / ratio || maxY > graph.getHeight() / ratio;

    if (tag) {
      break;
    }
  }

  return tag;
}

/**
 * 计算布局配置
 * @param dslType
 * @param nodes
 */
function calculateLayout(dslType: DslTypeEnum, nodes: NodeConfig[]): LayoutConfig {
  if (dslType === DslTypeEnum.WORKFLOW || nodes.length < 8) {
    return {
      type: 'dagre',
      rankdir: 'LR',
      // 节点间距（px）。在rankdir 为 'TB' 或 'BT' 时是节点的水平间距；在rankdir 为 'LR' 或 'RL' 时代表节点的竖直方向间距
      nodesep: 35,
      // 层间距（px）。在rankdir 为 'TB' 或 'BT' 时是竖直方向相邻层间距；在rankdir 为 'LR' 或 'RL' 时代表水平方向相邻层间距
      ranksep: 70,
      // 是否保留布局连线的控制点，默认false
      controlPoints: true,
    };
  }

  let cols = 3;
  if (nodes.length < 13) {
    cols = 3;
  } else if (nodes.length < 28) {
    cols = 5;
  } else {
    cols = 10;
  }
  let rows = Math.ceil(nodes.length / cols);

  if (rows % 2 === 0) {
    // 偶数行数时，强制转为奇数行数
    rows--;
    cols = Math.ceil(nodes.length / rows);
  }

  const arrList = nodes.split(cols);

  arrList.forEach((arr, iIndex) =>
    (iIndex % 2 === 0 ? arr : arr.reverse())
      .forEach((item, jIndex) => (item.degree = -1 * (iIndex * cols + jIndex))));

  return {
    type: 'grid',
    // 是否防止重叠，必须配合下面属性 nodeSize，只有设置了与当前图节点大小相同的 nodeSize 值，才能够进行节点重叠的碰撞检测
    preventOverlap: true,
    // 节点大小（直径）。用于防止节点重叠时的碰撞检测
    nodeSize: 60,
    // 避免重叠时节点的间距 padding。preventOverlap 为 true 时生效
    preventOverlapPadding: 130,
    cols,
    rows,
  };
}

// 最小缩放
export const MIN_ZOOM = 20;

/**
 * 适配到画布
 * @param graph
 */
export function fitCanvas(graph?: Graph): void {
  if (!graph) {
    return;
  }

  // 判断原始大小（100%）是否溢出
  if (!checkContentOverflow(graph)) {
    // 对齐到画布中心
    graph.fitCenter();
    return;
  }

  const minRatio = MIN_ZOOM / 100;

  // 缩放到最小，判断是否溢出
  if (checkContentOverflow(graph, minRatio)) {
    // 对齐到画布中心
    graph.fitCenter();

    // 溢出时，缩放到最小
    graph.zoomTo(minRatio, graph.getGraphCenterPoint());
    return;
  }

  // 没有溢出时，适配到画布中
  graph.fitView();
}

/**
 * 配置节点行为
 * @param graph
 * @param mouseoverNode
 */
export function configNodeAction(graph: undefined | Graph, mouseoverNode: ((evt: INodeMouseoverEvent) => void)): boolean {
  if (!graph) {
    return false;
  }

  // 设置鼠标滑过事件
  graph.on(G6Event.NODE_MOUSEOVER, (ev: IG6GraphEvent) => {
    const node = ev.item as Item;
    const model = node.getModel();

    if (ev.shape.get('name').includes('animate_')) {
      // 非异步任务或滑过动画相关shape时，忽略
      return;
    }

    const zoom = graph.getZoom();
    const width = size.width * zoom;
    const height = size.height * zoom;

    const { x, y } = graph.getClientByPoint(model.x as number, model.y as number);

    mouseoverNode({
      id: node.getID(),
      description: (model.description || '') as string,
      type: model.type as NodeTypeEnum,
      width,
      height,
      x,
      y,
    });
  });

  return true;
}

/**
 * 初始化
 * @param dsl
 * @param triggerType
 * @param nodeInfos
 * @param container
 */
export function init(dsl: string | undefined, triggerType: TriggerTypeEnum | undefined,
  nodeInfos: INodeDefVo[], container: HTMLElement | undefined): Graph | undefined {
  if (!dsl || !triggerType || !container) {
    return undefined;
  }

  const parentElement = container.parentElement as HTMLElement;

  const { dslType, nodes, edges } = parse(dsl, triggerType, nodeInfos);

  const graph = new Graph({
    modes: {
      default: [
        // 画布行为
        'drag-canvas',
        // 'scroll-canvas',
        // 'zoom-canvas',

        // 快捷键行为
        'shortcuts-call',
      ],
    },
    // 指定挂载容器
    container,
    // 图的宽度
    width: parentElement.clientWidth,
    // 图的高度
    height: parentElement.clientHeight,
    layout: calculateLayout(dslType, nodes),
  });

  // 加载数据
  graph.data({
    // 节点集
    nodes,
    // 边集
    edges,
  });

  container.style.visibility = 'hidden';

  // 渲染
  graph.render();

  setTimeout(() => {
    fitCanvas(graph);

    container.style.visibility = '';
  });

  return graph;
}

/**
 * 排序任务列表
 * @param tasks
 * @param desc
 * @param nodeName
 */
export function sortTasks(tasks: ITaskExecutionRecordVo[], desc: boolean, nodeName?: string): ITaskExecutionRecordVo[] {
  if (nodeName) {
    tasks = tasks.filter(task => task.nodeName === nodeName);
  }

  // 按开始时间降序排序
  return tasks.sort((t1, t2) => {
    const st1 = Date.parse(t1.startTime);
    const st2 = Date.parse(t2.startTime);
    if (st1 === st2) {
      return 0;
    }
    if (st1 > st2) {
      return desc ? -1 : 1;
    }
    return desc ? 1 : -1;
  });
}

/**
 * 更新节点状态
 * @param tasks
 * @param graph
 */
export function updateNodeStates(tasks: ITaskExecutionRecordVo[], graph?: Graph) {
  if (!graph) {
    return;
  }

  graph
    .getNodes()
    .filter(node => node.getModel().type === NodeTypeEnum.ASYNC_TASK)
    .forEach(node => {
      const task = sortTasks(tasks, true, node.getID())[0];
      const status = task ? task.status : TaskStatusEnum.INIT;

      graph.setItemState(node, 'status', status);
    });

  graph.getEdges().forEach(edge => {
    let status = edge.getSource().getStates().find(status => status === `status:${TaskStatusEnum.RUNNING}`);
    if (!status) {
      status = edge.getTarget().getStates().find(status => status === `status:${TaskStatusEnum.RUNNING}`);
    }

    graph.setItemState(edge, 'running', !!status);
  });
}