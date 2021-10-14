import { ISessionCreatingDto, ISessionVo } from '@/api/dto/session';
import { restProxy } from '@/api/index';

export const baseUrl = '/auth';

/**
 * 创建会话
 * @param dto
 */
export function create(dto: ISessionCreatingDto): Promise<ISessionVo> {
  return restProxy({
    url: `${baseUrl}/login`,
    method: 'post',
    payload: dto,
  });
}