import { restProxy } from '@/api';
import {
  ITriggerViewerDto,
  ITriggerWebhookVo,
  IWebRequestVo,
} from '@/api/dto/trigger';
import { IPageVo } from './dto/common';

export const baseUrl = {
  webhook: '/trigger/web_requests',
  trigger: '/view/trigger/webhook',
};

/**
 * 分页返回webhook请求列表
 */
export function getWebhookList(
  dto: ITriggerViewerDto
): Promise<IPageVo<IWebRequestVo>> {
  return restProxy({
    url: `${baseUrl.webhook}`,
    method: 'get',
    payload: dto,
    auth: true,
  });
}

/**
 * 获取WebHook Url
 */
export function getWebhookUrl(projectId: string): Promise<ITriggerWebhookVo> {
  return restProxy({
    url: `${baseUrl.trigger}/${projectId}`,
    method: 'get',
    auth: true,
  });
}
