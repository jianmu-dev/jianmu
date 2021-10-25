import { INodeInfoVo, ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { IProjectDetailVo } from '@/api/dto/project';
import { NodeToolbarTabTypeEnum } from '@/components/workflow/workflow-viewer/utils/enumeration';

/**
 * vuex状态
 */
export interface IState {
  recordDetail: {
    project?: IProjectDetailVo;
    navScrollLeft: number;
    allRecords: IWorkflowExecutionRecordVo[];
    record?: IWorkflowExecutionRecordVo;
    recordDsl?: string;
    taskRecords: ITaskExecutionRecordVo[];
    nodeInfos: INodeInfoVo[];
  };
}

/**
 * 打开任务日志表单
 */
export interface IOpenTaskLogForm {
  drawerVisible: boolean;
  id: string;
  tabType: NodeToolbarTabTypeEnum | '';
}

/**
 * 打开Webhook日志表单
 */
export interface IOpenWebhookLogForm {
  drawerVisible: boolean;
  id: string;
  tabType: NodeToolbarTabTypeEnum | '';
  triggerId?: string;
}