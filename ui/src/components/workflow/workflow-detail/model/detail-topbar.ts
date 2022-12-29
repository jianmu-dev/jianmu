import { executeImmediately } from '@/api/project';
import { terminateAll } from '@/api/workflow-execution-record';

type TriggerCallbackFnType = (msg: string, error?: Error) => void;

export class DeatilTopbar {
  private readonly projectId: string;
  private readonly workflowRef: string;
  private readonly triggerCallbackFn: TriggerCallbackFnType;
  constructor(projectId: string, workflowRef: string, triggerCallbackFn: TriggerCallbackFnType) {
    this.projectId = projectId;
    this.workflowRef = workflowRef;
    this.triggerCallbackFn = triggerCallbackFn;
  }
  /**
   * 终止所有流程
   */
  async terminateAllRecord(): Promise<void> {
    try {
      await terminateAll(this.workflowRef);
      this.triggerCallbackFn('操作成功，正在终止，请稍后');
    } catch (error: any) {
      if (error.response.status === 400) {
        console.log('触发新流程 error', error.response.data.message);
        this.triggerCallbackFn('', error);
      } else {
        this.triggerCallbackFn('', error);
      }
    }
  }
  /**
   * 触发新流程
   */
  async trigger(): Promise<void> {
    try {
      await executeImmediately(this.projectId);
      this.triggerCallbackFn('操作成功');
    } catch (error: any) {
      if (error.response.status === 400) {
        console.log('触发新流程 error', error.response.data.message);
        this.triggerCallbackFn('', error);
      } else {
        this.triggerCallbackFn('', error);
      }
    }
  }
}
