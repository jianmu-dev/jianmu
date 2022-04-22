import WorkflowGraph from './workflow-graph';
import WorkflowStencil from './workflow-stencil';
import { IWorkflowData } from './data';
import { Graph } from '@antv/x6';

export class WorkflowLayout {
  private readonly workflowGraph: WorkflowGraph;
  private readonly workflowStencil: WorkflowStencil;

  constructor(graphContainer: HTMLElement, stencilContainer: HTMLElement, data?: IWorkflowData) {
    // 初始化画布
    this.workflowGraph = new WorkflowGraph(graphContainer);
    // 初始化 stencil
    this.workflowStencil = new WorkflowStencil(this.workflowGraph.x6Graph, stencilContainer);
  }

  /**
   * 获取x6 graph对象
   */
  get x6Graph(): Graph {
    return this.workflowGraph.x6Graph;
  }
}