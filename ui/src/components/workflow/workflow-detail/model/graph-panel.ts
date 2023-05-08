import { IGlobalParamseterVo, INodeDefVo } from '@/api/dto/project';
import { ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { fetchWorkflow, getGlobalParameters, listAsyncTaskInstance } from '@/api/view-no-auth';

type DslCallbackFnType = (dslSourceCode: string, nodeInfos: INodeDefVo[]) => void;
type TaskCallbackFnType = (taskRecords: ITaskExecutionRecordVo[]) => void;
type GlobalParamsCallbackFnType = (globalParams: IGlobalParamseterVo[]) => void;

export class GraphPanel {
  private currentRecord: IWorkflowExecutionRecordVo;
  private readonly taskRecords: ITaskExecutionRecordVo[];
  private readonly dslCallbackFn: DslCallbackFnType;
  private readonly taskCallbackFn: TaskCallbackFnType;
  private readonly globalParamsCallbackFn: GlobalParamsCallbackFnType;
  constructor(
    currentRecord: IWorkflowExecutionRecordVo,
    dslCallbackFn: DslCallbackFnType,
    taskCallbackFn: TaskCallbackFnType,
    globalParamsCallbackFn: GlobalParamsCallbackFnType,
  ) {
    this.currentRecord = currentRecord;
    this.taskRecords = [];
    this.dslCallbackFn = dslCallbackFn;
    this.taskCallbackFn = taskCallbackFn;
    this.globalParamsCallbackFn = globalParamsCallbackFn;
    (async () => {
      await this.getTaskRecords();
    })();
    this.getGlobalParams();
  }
  async getGlobalParams() {
    if (!this.currentRecord.triggerId || this.currentRecord.status === 'INIT') {
      this.globalParamsCallbackFn([]);
      return;
    }
    const params: IGlobalParamseterVo[] = (await getGlobalParameters(this.currentRecord.triggerId)).map(e => {
      e.secretVisible = true;
      return e;
    });
    this.globalParamsCallbackFn(params);
  }
  async getDslAndNodeinfos() {
    if (!this.currentRecord.workflowRef) {
      return;
    }
    const { dslText: dslSourceCode, nodes } = await fetchWorkflow(
      this.currentRecord.workflowRef,
      this.currentRecord.workflowVersion,
    );
    const nodeInfos = nodes.filter(({ metadata }) => metadata).map(({ metadata }) => JSON.parse(metadata as string));
    this.dslCallbackFn(dslSourceCode, nodeInfos);
  }
  async getTaskRecords() {
    if (!this.currentRecord.triggerId) {
      this.taskCallbackFn([]);
      return;
    }
    const taskRecords = (await listAsyncTaskInstance(this.currentRecord.triggerId)).map(instance => {
      return {
        instanceId: '',
        businessId: instance.id,
        nodeName: instance.asyncTaskRef,
        defKey: instance.asyncTaskType,
        startTime: instance.startTime,
        endTime: instance.endTime,
        status: instance.status,
      };
    });
    this.taskRecords.length = 0;
    this.taskRecords.push(...taskRecords);
    this.taskCallbackFn(taskRecords);
  }
  refreshGparam(record: IWorkflowExecutionRecordVo) {
    this.currentRecord = record;
  }
}
