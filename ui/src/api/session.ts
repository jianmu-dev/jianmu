import {
  IAuthorizationUrlGettingDto,
  IAuthorizationUrlVo, IOauth2LoggingDto,
  ISessionCreatingDto,
  ISessionVo,
  IThirdPartyTypeVo,
} from '@/api/dto/session';
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

/**
 * 获取oauth三方登录方式
 */
export function fetchThirdPartyType() {
  return restProxy<IThirdPartyTypeVo>({
    url: `${baseUrl}/oauth2/third_party_type`,
    method: 'get',
  });
}

/**
 * oauth获取授权url
 */
export function fetchAuthUrl(dto: IAuthorizationUrlGettingDto) {
  return restProxy<IAuthorizationUrlVo>({
    url: `${baseUrl}/oauth2/url`,
    method: 'get',
    payload: dto,
  });
}

/**
 * oauth三方登录
 */
export function authLogin(dto: IOauth2LoggingDto) {
  return restProxy<ISessionVo>({
    url: `${baseUrl}/oauth2/login`,
    method: 'post',
    payload: dto,
  });
}
