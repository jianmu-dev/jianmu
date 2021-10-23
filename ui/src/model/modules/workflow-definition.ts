import { Mutable } from '@/utils/lib';
import { IGitCloningDto } from '@/api/dto/workflow-definition';
import { IProjectImportingDto, IProjectSavingDto } from '@/api/dto/project';

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
 * 导入表单
 */
export interface IImportForm extends Mutable<IProjectImportingDto> {
}