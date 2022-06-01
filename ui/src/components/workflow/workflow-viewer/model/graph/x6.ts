import { BaseGraph } from '../base-graph';
import { Graph, Node } from '@antv/x6';
import { parse } from '../dsl/x6';
import { TaskStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { WorkflowTool } from '@/components/workflow/workflow-editor/model/workflow-tool';
import { render } from '@/components/workflow/workflow-editor/model/workflow-graph';
import { CustomX6NodeProxy } from '@/components/workflow/workflow-editor/model/data/custom-x6-node-proxy';
import { NodeRefEnum, NodeTypeEnum, ZoomTypeEnum } from '@/components/workflow/workflow-editor/model/data/enumeration';
import { INodeMouseoverEvent } from '@/components/workflow/workflow-viewer/model/data/common';
import { NodeTypeEnum as G6NodeTypeEnum } from '../data/enumeration';
import { Cron } from '@/components/workflow/workflow-editor/model/data/node/cron';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { imgs, states } from '@/components/workflow/workflow-viewer/shapes/async-task';
import { BaseTaskRunning } from '../../animations/base-task-running';
import X6TaskRunning from '@/components/workflow/workflow-viewer/animations/task-running/x6';
import { checkDefaultIcon } from '@/components/workflow/workflow-editor/model/data/node/async-task';
import { NODE } from '@/components/workflow/workflow-editor/shape/gengral-config';

const { textMaxHeight } = NODE;

export class X6Graph extends BaseGraph {
  private readonly asyncTaskRefs: string[];
  private readonly graph: Graph;
  private readonly workflowTool: WorkflowTool;
  private readonly runningAnimations: Record<string, BaseTaskRunning>;

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

    this.runningAnimations = {};

    render(this.graph, data, this.workflowTool);

    this.graph.getNodes().forEach(node => {
      // 添加指示灯
      node.addTools({
        name: 'button',
        args: {
          markup: [
            {
              tagName: 'circle',
              selector: 'button',
              attrs: {
                r: 4,
                fill: states[TaskStatusEnum.INIT].indicatorStyle.fill,
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

  hideNodeToolbar(nodeRef: string): void {
    const node = this.getNodeByRef(nodeRef);
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

      const { id, description, type } = this.buildEvt(node);
      const { width, height, x, y } = shapeEl.getBoundingClientRect();
      mouseoverNode({ id, description, type, width, height: height - textMaxHeight, x, y });
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
    const completeNodes: Node[] = [];
    const runningNodes: Node[] = [];

    tasks.forEach(({ nodeName, status }) => {
      const index = this.asyncTaskRefs.indexOf(nodeName);
      if (index === -1) {
        return;
      }

      const node = nodes[index];
      this.refreshIndicator(node, status);

      const shapeEl = this.getShapeEl(node.id);

      const imgEl = shapeEl.querySelector('.img')! as HTMLImageElement;
      if (checkDefaultIcon(imgEl.style.backgroundImage)) {
        imgEl.style.backgroundImage = `url('${imgs[status]}')`;
      }

      const previousStatus = shapeEl.getAttribute('x6-task-status');
      if (status === TaskStatusEnum.RUNNING) {
        runningNodes.push(node);
      } else if (previousStatus === TaskStatusEnum.RUNNING) {
        completeNodes.push(node);
      }
      shapeEl.setAttribute('x6-task-status', status);
    });

    completeNodes.forEach(node => this.stopAnimation(node));
    runningNodes.forEach(node => this.startAnimation(node));
  }

  highlightNodeState(status: TaskStatusEnum, active: boolean, refreshing: boolean = false): void {
    super.highlightNodeState(status, active, refreshing);

    this.graph.getNodes().forEach(node => {
      const shapeEl = this.getShapeEl(node.id);
      if (shapeEl.getAttribute('x6-task-status') !== status) {
        return;
      }

      const imgEl = shapeEl.querySelector('.img')! as HTMLElement;
      imgEl.style.boxShadow = active ? `0 0 24px 1px ${states[status].indicatorStyle.fill}` : 'none';
    });
  }

  changeSize(width: number, height: number): void {
    this.graph.resizeGraph(width, height);
  }

  private getTaskNodes(): Node[] {
    const nodes: Node[] = [];
    let tempNode = this.graph.getRootNodes()[0];
    // eslint-disable-next-line no-constant-condition
    while (true) {
      if (!new CustomX6NodeProxy(tempNode).isTrigger()) {
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

  private getNodeByRef(nodeRef: string): Node {
    if ([NodeRefEnum.WEBHOOK, NodeRefEnum.CRON].includes(nodeRef as NodeRefEnum)) {
      return this.graph.getRootNodes()[0];
    }

    return this.getTaskNodes()[this.asyncTaskRefs.indexOf(nodeRef)];
  }

  private getShapeEl(nodeId: string): HTMLElement {
    return Array.from(this.graph.container.querySelectorAll('.jm-workflow-x6-vue-shape'))
      .filter(el => (el.getAttribute('data-x6-node-id') === nodeId))[0] as HTMLElement;
  }

  private refreshIndicator(node: Node, status: TaskStatusEnum): void {
    // 刷新指示灯
    const indicator = Array.from(this.graph.container.querySelectorAll('.x6-cell-tool.x6-node-tool.x6-cell-tool-button'))
      .filter(el => (el.getAttribute('data-cell-id') === node.id))[0] as SVGElement;
    const circle = (indicator.childNodes.item(0) as SVGElement);
    circle.setAttribute('fill', states[status].indicatorStyle.fill!);
  }

  private startAnimation(node: Node): void {
    let animation = this.runningAnimations[node.id];
    if (!animation) {
      animation = new X6TaskRunning(this.graph.findView(node)!);
      animation.start();
      this.runningAnimations[node.id] = animation;
    }

    const edges = this.graph.getIncomingEdges(node) || [];
    edges.push(...(this.graph.getOutgoingEdges(node) || []));

    edges.forEach(edge => {
      edge.setAttrByPath('line/strokeDasharray', '4, 2, 1, 2');
      edge.setAttrByPath('line/style/animation', 'x6-edge-running 10s linear infinite');
    });
  }

  private stopAnimation(node: Node): void {
    const animation = this.runningAnimations[node.id];
    if (animation) {
      animation.stop();
      delete this.runningAnimations[node.id];
    }

    const edges = this.graph.getIncomingEdges(node) || [];
    edges.push(...(this.graph.getOutgoingEdges(node) || []));

    edges.forEach(edge => {
      edge.removeAttrByPath('line/strokeDasharray');
      edge.removeAttrByPath('line/style/animation');
    });
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
      if (new CustomX6NodeProxy(tempNode).isTrigger()) {
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