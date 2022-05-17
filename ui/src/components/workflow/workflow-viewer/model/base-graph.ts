import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { INodeMouseoverEvent } from './data/common';
import { DslTypeEnum, TaskStatusEnum } from '@/api/dto/enumeration';
import { GraphDirectionEnum } from './data/enumeration';

interface Zoom {
  readonly min: number;
  readonly max: number;
  // 缩放间隔
  readonly interval: number;
}

export abstract class BaseGraph {
  protected readonly zoom: Zoom = { min: 20, max: 500, interval: 10 };
  readonly dslType: DslTypeEnum;
  // 当前高亮状态
  private highlightStatus?: TaskStatusEnum;

  protected constructor(dslType: DslTypeEnum) {
    this.dslType = dslType;
  }

  hideNodeToolbar(nodeId: string): void {
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
    this.highlightStatus = active ? status : undefined;
  }

  refreshNodeStateHighlight(status: TaskStatusEnum): void {
    const { highlightStatus } = this;
    if (!highlightStatus) {
      return;
    }

    if (highlightStatus !== status) {
      // 关灯
      this.highlightNodeState(status, false);
    }
    // 开灯
    this.highlightNodeState(highlightStatus, true);
  }

  changeSize(width: number, height: number): void {
  }

  destroy(): void {
  }
}