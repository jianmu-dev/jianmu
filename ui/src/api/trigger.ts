import { restProxy } from '@/api';
import {
  ITriggerViewingDto,
  IWebRequestVo,
  IWebhookParamVo,
  IWebRequestPayloadVo,
} from '@/api/dto/trigger';
import { IPageVo } from './dto/common';

export const baseUrl = {
  webhook: '/trigger/web_requests',
  retry: '/trigger/retry',
  trigger: '/trigger/web_requests',
  payload: '/trigger/web_requests',
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

/**
 * 获取webhook参数
 */
export function getWebhookParams(id: string): Promise<IWebhookParamVo> {
  return restProxy({
    url: `${baseUrl.trigger}/${id}/trigger`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取payload
 * @param id
 */
export function getPayloadParams(id: string): Promise<IWebRequestPayloadVo> {
  return restProxy({
    url: `${baseUrl.payload}/${id}/payload`,
    method: 'get',
    auth: true,
  });
}