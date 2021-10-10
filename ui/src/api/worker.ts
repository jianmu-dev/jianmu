import { restProxy } from '@/api/index';

export const baseUrl = '/workers';

/**
 * 获取worker类型列表
 */
export function listType(): Promise<string[]> {
  return restProxy({
    url: `${baseUrl}/types`,
    method: 'get',
    auth: true,
  });
}