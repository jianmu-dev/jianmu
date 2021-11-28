import { Mutable } from '@/utils/lib';
import { INamespaceSavingDto, INamespaceVo, ISecretKeyCreatingDto } from '@/api/dto/secret-key';
import { CredentialManagerTypeEnum } from '@/api/dto/enumeration';

/**
 * vuex状态
 */
export interface IState {
  credentialManagerType: CredentialManagerTypeEnum;
  namespaces: INamespaceVo[];
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