import { restProxy } from '@/api';
import { INodeCreatingDto } from '@/api/dto/node-library';

export const baseUrl = '/library';

/**
 * 创建节点
 * @param dto
 */
export function createNode(dto: INodeCreatingDto): Promise<void> {
  return restProxy<void>({
    url: `${baseUrl}/nodes`,
    method: 'post',
    payload: dto,
    auth: true,
  });
}

/**
 * 删除节点
 * @param ownerRef
 * @param ref
 */
export function deleteNodeLibrary(ownerRef: string, ref: string): Promise<void> {
  return restProxy<void>({
    url: `${baseUrl}/${ownerRef}/${ref}`,
    method: 'delete',
    auth: true,
  });
}

/**
 * 同步节点
 * @param ownerRef
 * @param ref
 */
export function syncNodeLibrary(ownerRef: string, ref: string): Promise<void> {
  return restProxy<void>({
    url: `${baseUrl}/${ownerRef}/${ref}`,
    method: 'put',
    timeout: 60 * 1000,
    auth: true,
  });
}