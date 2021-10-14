import { BaseVo, IPageDto } from '@/api/dto/common';

/**
 * 查询命名空间dto
 */
export interface INamespaceQueryingDto extends Readonly<IPageDto & {
  name?: string;
}> {
}

/**
 * 命名空间vo
 */
export interface INamespaceVo extends Readonly<BaseVo & {
  name: string;
  description?: string;
}> {
}

/**
 * 命名空间详情vo
 */
export interface INamespaceDetailVo extends INamespaceVo {
}

/**
 * 保存命名空间dto
 */
export interface INamespaceSavingDto extends Readonly<{
  name: string;
  description?: string;
}> {
}

/**
 * 创建密钥dto
 */
export interface ISecretKeyCreatingDto extends Readonly<{
  namespace: string;
  key: string;
  value: string;
}> {
}
