import { Mutable } from '@/utils/lib';
import { IGitCloningDto, IProjectImportingDto, IProjectQueryingDto, IProjectSavingDto } from '@/api/dto/project';

/**
 * vuex状态
 */
export interface IState {
}

/**
 * 查询表单
 */
export interface IQueryForm extends Mutable<IProjectQueryingDto> {
}

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