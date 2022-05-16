import { BaseGraph } from '../base-graph';
import { Graph } from '@antv/x6';
import { parse } from '../dsl/x6';
import { TriggerTypeEnum } from '@/api/dto/enumeration';
import { WorkflowTool } from '@/components/workflow/workflow-editor/model/workflow-tool';
import { render } from '@/components/workflow/workflow-editor/model/workflow-graph';

export class X6Graph extends BaseGraph {
  private readonly graph: Graph;
  private readonly workflowTool: WorkflowTool;

  constructor(dsl: string, triggerType: TriggerTypeEnum, container: HTMLElement) {
    const { dslType, data } = parse(dsl, triggerType);
    super(dslType);

    const containerParentEl = container.parentElement as HTMLElement;
    const { clientWidth: width, clientHeight: height } = containerParentEl;
    this.graph = new Graph({
      container,
      width,
      height,
      // 不绘制网格背景
      grid: false,
      // 定制节点和边的交互行为，false为禁用
      interacting: false,
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
}