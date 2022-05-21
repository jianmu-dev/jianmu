import { ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { INodeDefVo, IProjectDetailVo } from '@/api/dto/project';
import { NodeToolbarTabTypeEnum } from '@/components/workflow/workflow-viewer/model/data/enumeration';
import { TriggerTypeEnum } from '@/api/dto/enumeration';

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
    nodeInfos: INodeDefVo[];
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
  nodeName: string;
  tabType: NodeToolbarTabTypeEnum | '';
  triggerId?: string;
  triggerType?: TriggerTypeEnum;
}