import { Mutable } from '@/utils/lib';
import { IProjectQueryingDto } from '@/api/dto/project';

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