import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { INodeMouseoverEvent } from './data/common';
import { DslTypeEnum, TaskStatusEnum } from '@/api/dto/enumeration';
import { GraphDirectionEnum } from './data/enumeration';
import { INode } from '@antv/g6';

interface Zoom {
  readonly min: number;
  readonly max: number;
  // 缩放间隔
  readonly interval: number;
}

export abstract class BaseGraph {
  protected readonly zoom: Zoom = { min: 20, max: 500, interval: 10 };
  readonly dslType: DslTypeEnum;

  protected constructor(dslType: DslTypeEnum) {
    this.dslType = dslType;
  }

  hideNodeToolbar(nodeId: string): void{
  }

  getAsyncTaskNodeCount(): number {
    return 0;
  }

  getDirection(): GraphDirectionEnum {
    return GraphDirectionEnum.HORIZONTAL;
  }

  configNodeAction(mouseoverNode: ((evt: INodeMouseoverEvent) => void)): void {
  }

  zoomTo(factor: number): void {
  }

  getZoom(): number {
    return 1;
  }

  fitCanvas(): void {
  }

  fitView(): void {
  }

  updateNodeStates(tasks: ITaskExecutionRecordVo[]): void {
  }

  highlightNodeState(status: TaskStatusEnum, active: boolean): void {
  }

  refreshNodeStateHighlight(status: TaskStatusEnum): void {
  }

  changeSize(width: number, height: number): void {
  }

  destroy(): void {
  }
}