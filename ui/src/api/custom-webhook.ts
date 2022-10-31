import {
  INodeDefVersionListVo,
  IWebhookDefinitionVersionVo,
  IWebhookDefinitionVo,
  IWebhookOperatorVo,
} from './dto/custom-webhook';
import { restProxy } from '@/api/index';
import { API_PREFIX } from '@/utils/constants';

const baseUrl = {
  webhookOperatorUrl: `${API_PREFIX}/view/trigger/webhook/custom/operators`,
  webhookListUrl: `${API_PREFIX}/view/trigger/webhook/custom`,
  webhookVersionList: `${API_PREFIX}/view/trigger/webhook/custom`,
};

/**
 * 获取webhook运算符
 */
export function getWebhookOperators(): Promise<IWebhookOperatorVo> {
  return restProxy({
    url: `${baseUrl.webhookOperatorUrl}`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取webhookList
 */
export function getWebhookList(): Promise<IWebhookDefinitionVo[]> {
  return restProxy({
    url: `${baseUrl.webhookListUrl}`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取webhook version列表
 * @param ref
 * @param ownerRef
 */
export function getWebhookVersionList(ownerRef: string, ref: string): Promise<INodeDefVersionListVo> {
  return restProxy({
    url: `${baseUrl.webhookVersionList}/${ownerRef}/${ref}/versions`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取webhook version 某个版本具体参数
 * @param ownerRef
 * @param ref
 * @param version
 */
export function getWebhookVersionParams(
  ownerRef: string,
  ref: string,
  version: string,
): Promise<IWebhookDefinitionVersionVo> {
  return restProxy({
    url: `${baseUrl.webhookVersionList}/${ownerRef}/${ref}/${version}/versions`,
    method: 'get',
    auth: true,
  });
}
