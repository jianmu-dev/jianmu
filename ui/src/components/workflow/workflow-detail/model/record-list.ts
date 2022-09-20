import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { listWorkflowExecutionRecord } from '@/api/view-no-auth';

type RecordListCallbackFnType = (data: IWorkflowExecutionRecordVo[])=>void;

/**
 * @param workflowRef workflowRef
 * @param recordListCallbackFn record-list数据回调
*/
export class RecordList {
  private readonly workflowRef: string;
  private readonly recordListCallbackFn: RecordListCallbackFnType;
  private allRecords: IWorkflowExecutionRecordVo[];
  constructor(workflowRef: string, recordListCallbackFn: RecordListCallbackFnType) {
    this.workflowRef = workflowRef;
    this.recordListCallbackFn = recordListCallbackFn;
    this.allRecords = [];
  }

  /**
   * 获取所有record
   */
  async initAllRecords() {
    this.allRecords = await listWorkflowExecutionRecord(this.workflowRef);
    this.recordListCallbackFn([...this.allRecords]);
  }
}