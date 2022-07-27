import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { IProjectDetailVo } from '@/api/dto/project';
import { ViewModeEnum } from '@/api/dto/enumeration';

/**
 * 详情页面 地址栏参数 entry 适配gitLink(true)
 */
export interface IWorkflowDetailParam {
  entry: boolean;
  projectId: string;
  viewMode?: ViewModeEnum;
  triggerId?: string;
}

/**
 * 详情页面 当前项目 当前执行记录
 */
export interface IRecordDetail {
  project?: IProjectDetailVo;
  record?: IWorkflowExecutionRecordVo;
}
/**
 * record-list 参数
 */
export interface IRecordListParam {
  workflowRef: string;
  triggerId?: string;
}
