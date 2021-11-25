import { restProxy } from '@/api';
import { ITriggerViewingDto, IWebRequestVo } from '@/api/dto/trigger';
import { IPageVo } from './dto/common';

export const baseUrl = {
  webhook: '/trigger/web_requests',
  retry: '/trigger/retry',
};

/**
 * 分页返回webhook请求列表
 */
export function getWebhookList(
  dto: ITriggerViewingDto,
): Promise<IPageVo<IWebRequestVo>> {
  return restProxy({
    url: `${baseUrl.webhook}`,
    method: 'get',
    payload: dto,
    auth: true,
  });
}

/**
 * Webhook请求重试
 */
export function retryWebRequest(WebRequestId: string): Promise<void> {
  return restProxy({
    url: `${baseUrl.retry}/${WebRequestId}`,
    method: 'post',
    auth: true,
  });
}
