import { WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { listWorkflowExecutionRecord } from '@/api/view-no-auth';
import { refresh_seconds, refresh_times } from './util/consts';
import { checkWorkflowRunning } from './util/workflow';

type RecordListCallbackFnType = (data: IWorkflowExecutionRecordVo[])=>void;

/**
 * @param isConcurrent 是否并行
 * @param workflowRef workflowRef
 * @param recordListCallbackFn record-list数据回调
*/
export class RecordList {
  private isConcurrent: boolean;
  private ignoreSuspended: boolean = true;
  private ignoreTime: number = 0;
  private readonly workflowRef: string;
  private readonly recordListCallbackFn: RecordListCallbackFnType;
  private eventSource: any;
  private allRecords: IWorkflowExecutionRecordVo[];
  constructor(workflowRef: string, isConcurrent: boolean = false, recordListCallbackFn: RecordListCallbackFnType) {
    this.workflowRef = workflowRef;
    this.recordListCallbackFn = recordListCallbackFn;
    this.allRecords = [];
    this.isConcurrent = isConcurrent;
  }

  /**
   * 获取所有record
   */
  async initAllRecords() {
    this.allRecords = await listWorkflowExecutionRecord(this.workflowRef);
    this.recordListCallbackFn([...this.allRecords]);
  }
  refreshSuspended() {
    // 打开挂起刷新开关
    this.ignoreSuspended = false;
  }
  resetSuspended() {
    // 关闭挂起刷新开关
    this.ignoreTime = 0;
    this.ignoreSuspended = true;
  }
  /**
   * listen刷新数据
   */
  listen():void {
    this.eventSource = setInterval(async()=>{
      if (!this.allRecords.length) {
        console.debug('allRecords -> 0,不监听record-list状态');
        return;
      }
      // 是串行且有挂起->阻止刷新
      if (!this.isConcurrent && this.allRecords.find(e => e.status === WorkflowExecutionRecordStatusEnum.SUSPENDED)) {
        return;
      }
      // 全量判断是否刷新
      if (!this.allRecords.find(item => checkWorkflowRunning(item.status, this.ignoreSuspended))) {
        // 忽略挂起刷新状态(次数) 重置
        console.debug('忽略record-list挂起 重置');
        return;
      }
      // 不忽略挂起刷新次数 新增
      if (!this.ignoreSuspended) {
        this.ignoreTime++;
        console.debug(`graph 重试(忽略)后刷新第${this.ignoreTime}次`);
      }
      // 不忽略挂起刷新次数->重置 忽略挂起状态->重置
      if (this.ignoreTime >= refresh_times) {
        this.ignoreTime = 0;
        this.ignoreSuspended = true;
      }
      console.debug('实例列表中有init running 或 重试 刷新实例列表', this.ignoreSuspended);
      try {
        await this.initAllRecords();
      } catch (e: any) {
        // 捕获异常
        console.warn(e.message, e);
      }
    }, refresh_seconds);
  }

  /**
   * 销毁
   */
  destroy(): void {
    this.eventSource && clearInterval(this.eventSource);
  }
  /**
   * TODO -> SSE
   */
  // private executeSSE() {
  //   this.eventSource = new EventSource(`/view/workflow_instances/${this.workflowRef}`, { withCredentials: true });
  //   this.eventSource.onmessage = async (e: any) => {
  //     this.recordListCallbackFn([...this.allRecords]);
  //   };
  //   this.eventSource.onerror = (e: any) => {
  //     if (e.currentTarget.readyState === 0) {
  //       console.log('服务端已断开连接，禁止尝试重连');
  //       this.eventSource.close();
  //       return;
  //     }
  //     console.error(e);
  //   };
  // }
}