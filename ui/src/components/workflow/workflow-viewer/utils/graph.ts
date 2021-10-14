import { G6Event, Graph, IBBox, IG6GraphEvent, Item } from '@antv/g6';
import { parse } from './dsl';
import { NodeTypeEnum } from './enumeration';
import { TaskStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { INodeInfoVo, ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { size } from '@/components/workflow/workflow-viewer/shapes/async-task';
import { INodeMouseoverEvent } from '@/components/workflow/workflow-viewer/utils/model';

/**
 * 检查内容是否溢出
 * @param graph
 */
function checkContentOverflow(graph: Graph): boolean {
  const bBoxes: IBBox[] = [];
  bBoxes.push(...graph.getNodes().map(node => node.getCanvasBBox()));
  bBoxes.push(...graph.getEdges().map(node => node.getCanvasBBox()));

  // 初始未溢出
  let tag = false;

  for (const { maxX, maxY } of bBoxes) {
    tag = maxX > graph.getWidth() || maxY > graph.getHeight();

    if (tag) {
      break;
    }
  }

  return tag;
}

/**
 * 适配到画布
 * @param graph
 */
export function fitCanvas(graph: Graph): void {
  if (!graph) {
    return;
  }

  if (checkContentOverflow(graph)) {
    // 适配到画布中
    graph.fitView();
  } else {
    // 对齐到画布中心
    graph.fitCenter();
  }
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
  nodeInfos: INodeInfoVo[], container: HTMLElement): Graph | undefined {
  if (!dsl || !triggerType) {
    return undefined;
  }

  const parentElement = container.parentElement as HTMLElement;

  const { nodes, edges } = parse(dsl, triggerType, nodeInfos);

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
    layout: {
      type: 'dagre',
      rankdir: 'LR',
      // 节点间距（px）。在rankdir 为 'TB' 或 'BT' 时是节点的水平间距；在rankdir 为 'LR' 或 'RL' 时代表节点的竖直方向间距
      nodesep: 60,
      // 层间距（px）。在rankdir 为 'TB' 或 'BT' 时是竖直方向相邻层间距；在rankdir 为 'LR' 或 'RL' 时代表水平方向相邻层间距
      ranksep: 60,
      // 是否保留布局连线的控制点，默认false
      controlPoints: true,
    },
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
 * 更新节点状态
 * @param tasks
 * @param graph
 */
export function updateNodeStates(tasks: ITaskExecutionRecordVo[], graph: Graph) {
  graph
    .getNodes()
    .filter(node => node.getModel().type === NodeTypeEnum.ASYNC_TASK)
    .forEach(node => {
      const task = tasks.find(task => task.nodeName === node.getID());
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