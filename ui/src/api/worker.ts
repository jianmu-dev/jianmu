import { restProxy } from '@/api/index';
import { API_PREFIX } from '@/utils/constants';

export const baseUrl = `${API_PREFIX}/workers`;

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
