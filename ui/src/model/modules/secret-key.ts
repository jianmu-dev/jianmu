import { Mutable } from '@/utils/lib';
import { INamespaceQueryingDto, INamespaceSavingDto, INamespaceVo, ISecretKeyCreatingDto } from '@/api/dto/secret-key';

/**
 * vuex状态
 */
export interface IState {
  totalPages: number;
  totalElements: number;
  namespaces: INamespaceVo[];
}

/**
 * 查询命名空间表单
 */
export interface IQueryNamespaceForm extends Mutable<INamespaceQueryingDto> {
}

/**
 * 保存命名空间表单
 */
export interface ISaveNamespaceForm extends Mutable<INamespaceSavingDto> {
}

/**
 * 创建密钥表单
 */
export interface ICreateSecretKeyForm extends Mutable<ISecretKeyCreatingDto> {
}