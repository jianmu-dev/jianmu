import { BaseGraph } from './base-graph';
import { G6Graph } from './graph/g6';
import { TriggerTypeEnum } from '@/api/dto/enumeration';
import { INodeDefVo } from '@/api/dto/project';
import { GraphDirectionEnum } from './data/enumeration';

export class WorkflowGraph {
  readonly graph: BaseGraph
  private readonly resizeObserver: ResizeObserver;

  constructor(dsl: string, triggerType: TriggerTypeEnum,
    nodeInfos: INodeDefVo[], container: HTMLElement, direction: GraphDirectionEnum) {
    this.graph = new G6Graph(dsl, triggerType, nodeInfos, container, direction);

    const parentElement = container.parentElement as HTMLElement;
    this.resizeObserver = new ResizeObserver(() => {
      this.graph.changeSize(parentElement.clientWidth, parentElement.clientHeight);
    });
    // 监控容器大小变化
    this.resizeObserver.observe(parentElement);
  }

  destroy() {
    // 销毁监控容器大小变化
    this.resizeObserver.disconnect();

    // 销毁画布
    this.graph.destroy();
  }
}