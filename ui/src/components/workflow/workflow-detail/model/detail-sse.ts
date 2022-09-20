import { IEvent } from '@/api/event/common';

type RecordListCallbackFnType = (event: IEvent)=>void;

/**
 * @param workflowRef workflowRef
 * @param eventCallbackFn 事件以及数据回调
*/
export class WorkflowDetail {
  private readonly workflowRef: string;
  private readonly eventCallbackFn: RecordListCallbackFnType;
  private eventSource: any;
  constructor(workflowRef: string, eventCallbackFn: RecordListCallbackFnType) {
    this.workflowRef = workflowRef;
    this.eventCallbackFn = eventCallbackFn;
    this.listen();
  }
  /**
   * listen刷新数据
   */
  listen() {
    this.eventSource = new EventSource(`/view/workflow_instance/subscribe/${this.workflowRef}`, { withCredentials: true });
    this.eventSource.onmessage = async ({ data }:any) => {
      const changData = JSON.parse(data);
      this.eventCallbackFn(changData);
    };
    this.eventSource.onerror = (e: any) => {
      console.error(e);
    };
  }
  /**
   * 销毁
   */
  destroy(): void {
    this.eventSource && this.eventSource.close();
  }
}