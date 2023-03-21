export const baseUrl = '/caches';
import { restProxy } from '@/api/index';
import { API_PREFIX } from '@/utils/constants';

/**
 * 清理缓存
 * @param cacheId
 */
export function clearCache(cacheId: string): Promise<void> {
  return restProxy<void>({
    url: `${API_PREFIX}/${baseUrl}/${cacheId}`,
    method: 'put',
    auth: true,
  });
}
