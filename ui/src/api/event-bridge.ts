import { restProxy } from '@/api/index';
import { IEventBridgeDetailVo, IEventBridgeSavingDto } from '@/api/dto/event-bridge';
import { IProjectWebhookVo } from '@/api/dto/project';

export const baseUrl = '/eb';

/**
 * 保存事件桥接器
 * @param dto
 */
export function saveEventBridge(dto: IEventBridgeSavingDto): Promise<IEventBridgeDetailVo> {
  return restProxy<IEventBridgeDetailVo>({
    url: baseUrl,
    method: 'put',
    payload: dto,
    auth: true,
  });
}

/**
 * 删除事件桥接器
 * @param bridgeId
 */
export function deleteEventBridge(bridgeId: string): Promise<void> {
  return restProxy<void>({
    url: `${baseUrl}/${bridgeId}`,
    method: 'delete',
    auth: true,
  });
}

/**
 * 获取webhook
 * @param sourceId
 */
export function fetchWebhook(sourceId: string): Promise<IProjectWebhookVo> {
  return restProxy({
    url: `${baseUrl}/webhook/${sourceId}`,
    method: 'get',
    auth: true,
  });
}

/**
 * 重新生成webhook
 * @param sourceId
 */
export function regenerateWebhook(sourceId: string): Promise<IProjectWebhookVo> {
  return restProxy({
    url: `${baseUrl}/webhook/${sourceId}`,
    method: 'patch',
    auth: true,
  });
}