import { ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { INodeDefVo, IProjectDetailVo } from '@/api/dto/project';
import { ViewModeEnum } from '@/api/dto/enumeration';
// import { IAssociation } from '@/components/workflow/workflow-editor/model/data/common';
/**
 * 详情页面 地址栏参数 todo triggerId -> triggerId
 */
export interface IWorkflowDetailParam {
  entry: boolean;
  projectId: string;
  viewMode?: ViewModeEnum;
  triggerId?: string;
}

/**
 * 详情页面 具体实例
 */
export interface IRecordDetail {
  project?: IProjectDetailVo;
  record?: IWorkflowExecutionRecordVo;
}
/**
 * list 参数
 */
export interface IRecordListParam {
  workflowRef: string;
  triggerId?: string;
}
/**
 * 详情页面数据
 */
export interface IWorkflowDetail {
  project: IProjectDetailVo;
  navScrollLeft: number;
  allRecords: IWorkflowExecutionRecordVo[];
  record: IWorkflowExecutionRecordVo;
  recordDsl: string;
  taskRecords: ITaskExecutionRecordVo[];
  nodeInfos: INodeDefVo[];
}