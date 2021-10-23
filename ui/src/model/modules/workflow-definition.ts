import { Mutable } from '@/utils/lib';
import {
  IGitCloningDto,
  IWorkflowDefinitionImportingDto,
  IWorkflowDefinitionSavingDto,
} from '@/api/dto/workflow-definition';

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