import { IEvent } from '@/api/event/common';
import { API_PREFIX } from '@/utils/constants';

type RecordListCallbackFnType = (event: IEvent) => void;

/**
 * @param workflowRef workflowRef
 * @param eventCallbackFn 事件以及数据回调
 */
export class WorkflowDetail {
  private readonly workflowRef: string;
  private readonly eventCallbackFn: RecordListCallbackFnType;
  private readonly call: () => void;
  private eventSource: any;

  constructor(workflowRef: string, eventCallbackFn: RecordListCallbackFnType, call: () => void) {
    this.workflowRef = workflowRef;
    this.eventCallbackFn = eventCallbackFn;
    this.call = call;
    this.listen();
  }

  /**
   * listen刷新数据
   */
  listen() {
    this.eventSource = new EventSource(`${API_PREFIX}/view/workflow_instance/subscribe/${this.workflowRef}`, {
      withCredentials: true,
    });
    this.eventSource.onmessage = async ({ data }: any) => {
      const changData = JSON.parse(data);
      this.eventCallbackFn(changData);
    };
    this.eventSource.onopen = () => {
      this.call();
    };
  }

  /**
   * 销毁
   */
  destroy(): void {
    if (this.eventSource) {
      this.eventSource.close();
    }
  }
}
