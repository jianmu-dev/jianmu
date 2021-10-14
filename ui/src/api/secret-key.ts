import { restProxy } from '@/api';
import { INamespaceSavingDto, ISecretKeyCreatingDto } from '@/api/dto/secret-key';

export const baseUrl = '/secrets/namespaces';

/**
 * 保存命名空间
 * @param dto
 */
export function saveNamespace(dto: INamespaceSavingDto): Promise<void> {
  return restProxy<void>({
    url: baseUrl,
    method: 'post',
    payload: dto,
    auth: true,
  });
}

/**
 * 删除命名空间
 * @param name
 */
export function deleteNamespace(name: string): Promise<void> {
  return restProxy<void>({
    url: `${baseUrl}/${name}`,
    method: 'delete',
    auth: true,
  });
}

/**
 * 创建密钥
 * @param namespace
 * @param key
 * @param value
 */
export function createSecretKey({ namespace, key, value }: ISecretKeyCreatingDto): Promise<void> {
  return restProxy<void>({
    url: `${baseUrl}/${namespace}`,
    method: 'post',
    payload: { key, value },
    auth: true,
  });
}

/**
 * 删除密钥
 * @param namespace
 * @param key
 */
export function deleteSecretKey(namespace: string, key: string): Promise<void> {
  return restProxy<void>({
    url: `${baseUrl}/${namespace}/${key}`,
    method: 'delete',
    auth: true,
  });
}