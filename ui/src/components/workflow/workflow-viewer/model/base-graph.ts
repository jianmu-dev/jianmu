import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { INodeMouseoverEvent } from './data/common';
import { DslTypeEnum, TaskStatusEnum } from '@/api/dto/enumeration';
import { GraphDirectionEnum, GraphTypeEnum } from './data/enumeration';
import { G6Graph } from './graph/g6';

interface Zoom {
  readonly min: number;
  readonly max: number;
  // 缩放间隔
  readonly interval: number;
}

export abstract class BaseGraph {
  protected readonly zoom: Zoom = { min: 20, max: 500, interval: 10 };
  readonly dslType: DslTypeEnum;
  // 刷新当前高亮状态监听器
  private highlightRefreshingListener?: any;

  protected constructor(dslType: DslTypeEnum) {
    this.dslType = dslType;
  }

  getGraphType(): GraphTypeEnum {
    return this instanceof G6Graph ? GraphTypeEnum.G6 : GraphTypeEnum.X6;
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

  highlightNodeState(status: TaskStatusEnum, active: boolean, refreshing: boolean = false): void {
    if (refreshing) {
      // 刷新时，忽略，不会改变当前高亮状态
      return;
    }

    if (active) {
      this.highlightRefreshingListener = setInterval(() => {
        Object.values(TaskStatusEnum).forEach(item => {
          if (item === status) {
            // 开灯
            this.highlightNodeState(item, true, true);
            return;
          }
          // 关灯
          this.highlightNodeState(item, false, true);
        });
      }, 500);
      return;
    }

    if (!this.highlightRefreshingListener) {
      return;
    }
    clearInterval(this.highlightRefreshingListener);
    delete this.highlightRefreshingListener;
  }

  changeSize(width: number, height: number): void {
  }

  destroy(): void {
  }
}