import { Mutable } from '@/utils/lib';
import {
  IGitCloningDto,
  IWorkflowDefinitionImportingDto,
  IWorkflowDefinitionQueryingDto,
  IWorkflowDefinitionSavingDto,
  IWorkflowDefinitionVo,
} from '@/api/dto/workflow-definition';

/**
 * vuex状态
 */
export interface IState {
  totalPages: number;
  totalElements: number;
  definitions: IWorkflowDefinitionVo[];
}

/**
 * 查询表单
 */
export interface IQueryForm extends Mutable<IWorkflowDefinitionQueryingDto> {
}

/**
 * 保存表单
 */
export interface ISaveForm extends Mutable<IWorkflowDefinitionSavingDto> {
}

/**
 * Git克隆表单
 */
export interface IGitCloneForm extends Mutable<IGitCloningDto> {
}

/**
 * Git克隆表单
 */
export interface IImportForm extends Mutable<IWorkflowDefinitionImportingDto> {
}