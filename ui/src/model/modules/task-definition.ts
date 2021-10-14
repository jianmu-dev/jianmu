import {
  ITaskDefinitionCreatingDto,
  ITaskDefinitionQueryingDto,
  ITaskDefinitionVersionVo,
  ITaskDefinitionVo,
} from '@/api/dto/task-definition';
import { Mutable } from '@/utils/lib';

/**
 * vuex状态
 */
export interface IState {
  totalPages: number;
  totalElements: number;
  definitions: ITaskDefinitionVo[];
  versions: ITaskDefinitionVersionVo[][];
}

/**
 * 查询表单
 */
export interface IQueryForm extends Mutable<ITaskDefinitionQueryingDto> {
}

/**
 * 创建表单
 */
export interface ICreateForm extends Mutable<ITaskDefinitionCreatingDto> {
}

/**
 * 删除版本表单
 */
export interface IDeleteVersionForm {
  taskDefRef: string;
  taskDefVersion: string;
}