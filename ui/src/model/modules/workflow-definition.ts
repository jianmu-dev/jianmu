import { Mutable } from '@/utils/lib';
import { IGitCloningDto, IWorkflowDefinitionImportingDto } from '@/api/dto/workflow-definition';
import { IProjectSavingDto } from '@/api/dto/project';

/**
 * 保存表单
 */
export interface ISaveForm extends Mutable<IProjectSavingDto> {
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