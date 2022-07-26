import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { listWorkflowExecutionRecord } from '@/api/view-no-auth';
import { checkWorkflowRunning } from './utils/workflow';

type RecordListCallbackFnType = (data: IWorkflowExecutionRecordVo[])=>void;

/**
 * @param sseUrl sse接口地址 空字符默认为不执行SSE接口
 * @param workflowRef sse接口地址(或普通接口地址)后面拼接的 workflowRef
 * @param recordListCallbackFn 追加新数据的回调方法
*/
export class RecordList {
  private ignoreSuspended: boolean = true;
  private ignoreTime: number = 0;
  private readonly workflowRef: string;
  private readonly recordListCallbackFn: RecordListCallbackFnType;
  private eventSource: any;
  private allRecords: IWorkflowExecutionRecordVo[];
  constructor(workflowRef: string, recordListCallbackFn: RecordListCallbackFnType) {
    this.workflowRef = workflowRef;
    this.recordListCallbackFn = recordListCallbackFn;
    this.allRecords = [];
  }

  /**
   * 不执行SSE
  */
  async initAllRecords() {
    this.allRecords = await listWorkflowExecutionRecord(this.workflowRef);
    this.recordListCallbackFn([...this.allRecords]);
  }
  refreshSuspended() {
    // 挂起状态刷新
    this.ignoreSuspended = false;
  }
  resetSuspended() {
    this.ignoreSuspended = true;
  }
  /**
   * listen刷新数据
  */
  listen():void {
    this.eventSource = setInterval(async()=>{
      // console.log('record-list 3秒', this.allRecords.length);
      if (!this.allRecords.length) {
        console.log('allRecords -> 0,不监听record-list状态');
        return;
      }
      // 全量判断是否刷新
      if (!this.allRecords.find(item => checkWorkflowRunning(item.status, this.ignoreSuspended))) {
        // 忽略挂起刷新状态(次数) 重置
        console.log('忽略record-list挂起 重置');
        // console.log('忽略record-list挂起 重置', this.allRecords.map(e=>e.status));
        return;
      }
      // 不忽略挂起刷新次数 新增
      if (!this.ignoreSuspended) {
        // this.ignoreSuspended -> false
        this.ignoreTime++;
        console.log(`graph 重试(忽略)后刷新第${this.ignoreTime}次`);
      }
      // 不忽略挂起刷新次数->重置 忽略挂起状态->重置
      if (this.ignoreTime >= 20) {
        this.ignoreTime = 0;
        this.ignoreSuspended = true;
      }
      console.log('实例列表中有init running 或 重试 刷新实例列表', this.ignoreSuspended);
      try {
        await this.initAllRecords();
      } catch (e: any) {
        // 捕获异常
        console.warn(e.message, e);
      }
    }, 3000);
  }
  /**
   * 执行SSE
  */
  private executeSSE() {
    this.eventSource = new EventSource(`/view/workflow_instances/${this.workflowRef}`, { withCredentials: true });
    this.eventSource.onmessage = async (e: any) => {
      // this.allRecords e 数据处理
      this.recordListCallbackFn([...this.allRecords]);
    };
    this.eventSource.onerror = (e: any) => {
      if (e.currentTarget.readyState === 0) {
        console.log('服务端已断开连接，禁止尝试重连');
        this.eventSource.close();
        return;
      }
      console.error(e);
    };
  }

  /**
   * 销毁
   */
  destroy(): void {
    // this.eventSource && this.eventSource.close();
    this.eventSource && clearInterval(this.eventSource);
  }
}