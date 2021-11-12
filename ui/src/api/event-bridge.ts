import { restProxy } from '@/api/index';
import { IEventBridgeDetailVo, IEventBridgeSavingDto } from '@/api/dto/event-bridge';

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