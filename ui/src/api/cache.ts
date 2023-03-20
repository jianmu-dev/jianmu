export const baseUrl = '/caches';
import { restProxy } from '@/api/index';

/**
 * 清理缓存
 * @param cacheId
 */
export function clearCache(cacheId: string): Promise<void> {
  return restProxy<void>({
    url: `${baseUrl}/${cacheId}`,
    method: 'put',
    auth: true,
  });
}
