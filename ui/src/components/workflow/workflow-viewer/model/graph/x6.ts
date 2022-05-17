import { BaseGraph } from '../base-graph';
import { Graph, Node } from '@antv/x6';
import { parse } from '../dsl/x6';
import { TaskStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { WorkflowTool } from '@/components/workflow/workflow-editor/model/workflow-tool';
import { render } from '@/components/workflow/workflow-editor/model/workflow-graph';
import { CustomX6NodeProxy } from '@/components/workflow/workflow-editor/model/data/custom-x6-node-proxy';
import { NodeTypeEnum, ZoomTypeEnum } from '@/components/workflow/workflow-editor/model/data/enumeration';
import { INodeMouseoverEvent } from '@/components/workflow/workflow-viewer/model/data/common';
import { NodeTypeEnum as G6NodeTypeEnum } from '../data/enumeration';
import { Cron } from '@/components/workflow/workflow-editor/model/data/node/cron';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { states } from '@/components/workflow/workflow-viewer/shapes/async-task';

export class X6Graph extends BaseGraph {
  private readonly asyncTaskRefs: string[];
  private readonly graph: Graph;
  private readonly workflowTool: WorkflowTool;

  constructor(dsl: string, triggerType: TriggerTypeEnum, container: HTMLElement) {
    const { dslType, asyncTaskRefs, data } = parse(dsl, triggerType);
    super(dslType);
    this.asyncTaskRefs = asyncTaskRefs;

    const containerParentEl = container.parentElement as HTMLElement;
    const { clientWidth: width, clientHeight: height } = containerParentEl;
    this.graph = new Graph({
      container,
      width,
      height,
      // 可拖拽平移
      panning: true,
      // 不绘制网格背景
      grid: false,
      // 定制节点和边的交互行为，false为禁用
      interacting: {
        edgeMovable: false,
        edgeLabelMovable: false,
        arrowheadMovable: false,
        vertexMovable: false,
        vertexAddable: false,
        vertexDeletable: false,
        useEdgeTools: false,
        nodeMovable: false,
        magnetConnectable: false,
        stopDelegateOnDragging: false,
        // 可添加工具，用于指示灯
        toolsAddable: true,
      },
      // 缩放节点，默认禁用。
      resizing: false,
      // 旋转节点，默认禁用
      rotating: false,
      // 点选/框选，默认禁用
      selecting: false,
      // 	对齐线，默认禁用。
      snapline: false,
      // 	键盘快捷键，默认禁用。
      keyboard: false,
      // 剪切板，默认禁用。
      clipboard: false,
    });

    // 初始化工具
    this.workflowTool = new WorkflowTool(this.graph);

    render(this.graph, data, this.workflowTool);
  }

  hideNodeToolbar(asyncTaskRef: string): void {
    const node = this.getNodeByAsyncTaskRef(asyncTaskRef);
    const imgEl = this.getShapeEl(node.id).querySelector('.img')! as HTMLElement;
    imgEl.style.boxShadow = '';
  }

  getAsyncTaskNodeCount(): number {
    return this.graph.getNodes()
      .filter(node => [NodeTypeEnum.SHELL, NodeTypeEnum.ASYNC_TASK]
        .includes(new CustomX6NodeProxy(node).getData().getType()))
      .length;
  }

  configNodeAction(mouseoverNode: ((evt: INodeMouseoverEvent) => void)): void {
    // 设置鼠标滑过事件
    this.graph.on('node:mouseenter', ({ node }) => {
      const shapeEl = this.getShapeEl(node.id);
      // 鼠标进入节点时，显示阴影
      (shapeEl.querySelector('.img')! as HTMLElement)
        .style.boxShadow = '0 0 8px 1px #C5D9FF';

      // TODO 非异步任务或滑过动画相关shape时，忽略

      const { id, description, type } = this.buildEvt(node);
      const { width, height, x, y } = shapeEl.getBoundingClientRect();
      mouseoverNode({ id, description, type, width, height, x, y });
    });
  }

  zoomTo(factor: number): void {
    if (factor === 100) {
      this.workflowTool.zoom(ZoomTypeEnum.ORIGINAL);
      return;
    }

    const curFactor = Math.round(this.graph.zoom() * 100);
    if (factor > curFactor) {
      this.workflowTool.zoom(ZoomTypeEnum.IN);
      return;
    }

    if (factor < curFactor) {
      this.workflowTool.zoom(ZoomTypeEnum.OUT);
      return;
    }
  }

  getZoom(): number {
    return this.graph.zoom();
  }

  fitCanvas(): void {
    this.workflowTool.zoom(ZoomTypeEnum.CENTER);
  }

  fitView(): void {
    this.workflowTool.zoom(ZoomTypeEnum.FIT);
  }

  updateNodeStates(tasks: ITaskExecutionRecordVo[]): void {
    const nodes = this.getTaskNodes();

    tasks.forEach(({ nodeName, status }) => {
      const index = this.asyncTaskRefs.indexOf(nodeName);
      if (index === -1) {
        return;
      }

      const node = nodes[index];
      // 移除指示灯
      node.removeTools();
      // 添加新指示灯
      node.addTools({
        name: 'button',
        args: {
          markup: [
            {
              tagName: 'circle',
              selector: 'button',
              attrs: {
                r: 4,
                fill: states[status].indicatorStyle.fill,
                cursor: 'default',
              },
            },
          ],
          x: '100%',
          y: 0,
          offset: { x: 4, y: 4 },
        },
      });
    });
  }

  highlightNodeState(status: TaskStatusEnum, active: boolean): void {
  }

  refreshNodeStateHighlight(status: TaskStatusEnum): void {
  }

  changeSize(width: number, height: number): void {
    this.graph.resizeGraph(width, height);
  }

  private getTaskNodes(): Node[] {
    const nodes: Node[] = [];
    let tempNode = this.graph.getRootNodes()[0];
    // eslint-disable-next-line no-constant-condition
    while (true) {
      const tempWorkflowNode = new CustomX6NodeProxy(tempNode).getData();
      if (![NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(tempWorkflowNode.getType())) {
        nodes.push(tempNode);
      }

      const edges = this.graph.getOutgoingEdges(tempNode);
      if (!edges) {
        break;
      }

      tempNode = edges[0].getTargetNode()!;
    }

    return nodes;
  }

  private getNodeByAsyncTaskRef(asyncTaskRef: string) {
    return this.getTaskNodes()[this.asyncTaskRefs.indexOf(asyncTaskRef)];
  }

  private getShapeEl(nodeId: string): HTMLElement {
    return Array.from(this.graph.container.querySelectorAll('.jm-workflow-x6-vue-shape'))
      .filter(el => (el.getAttribute('data-x6-node-id') === nodeId))[0] as HTMLElement;
  }

  private buildEvt(node: Node): {
    id: string; description: string, type: G6NodeTypeEnum;
  } {
    const workflowNode = new CustomX6NodeProxy(node).getData();
    const description = workflowNode.getName();
    const type = (workflowNode.getType() === NodeTypeEnum.SHELL ? 'async-task' : workflowNode.getType()) as G6NodeTypeEnum;

    if (workflowNode.getType() === NodeTypeEnum.CRON) {
      return {
        id: workflowNode.getRef(),
        description: (workflowNode as Cron).schedule,
        type,
      };
    }
    if (workflowNode.getType() === NodeTypeEnum.WEBHOOK) {
      return { id: workflowNode.getRef(), description, type };
    }

    let index = 0;
    let tempNode = this.graph.getRootNodes()[0];
    // eslint-disable-next-line no-constant-condition
    while (true) {
      const tempWorkflowNode = new CustomX6NodeProxy(tempNode).getData();
      if ([NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(tempWorkflowNode.getType())) {
        tempNode = this.graph.getOutgoingEdges(tempNode)![0].getTargetNode()!;
        continue;
      }

      if (tempNode.id === node.id) {
        break;
      }

      tempNode = this.graph.getOutgoingEdges(tempNode)![0].getTargetNode()!;
      index++;
    }

    return {
      id: this.asyncTaskRefs[index],
      description,
      type,
    };
  }
}