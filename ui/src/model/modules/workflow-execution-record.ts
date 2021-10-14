import { INodeInfoVo, ITaskExecutionRecordVo, IWorkflowExecutionRecordQueryingDto, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { Mutable } from '@/utils/lib';
import { IPageVo } from '@/api/dto/common';
import { IProjectDetailVo } from '@/api/dto/project';
import { NodeToolbarTabTypeEnum } from '@/components/workflow/workflow-viewer/utils/enumeration';

/**
 * vuex状态
 */
export interface IState {
  totalElements: {
    executing: number;
    completed: number;
  };
  executing: IPageVo<IWorkflowExecutionRecordVo>;
  completed: IPageVo<IWorkflowExecutionRecordVo>;
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
 * 查询表单
 */
export interface IQueryForm extends Mutable<IWorkflowExecutionRecordQueryingDto> {
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