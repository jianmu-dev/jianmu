import {
  IAuthorizationUrlGettingDto,
  IAuthorizationUrlVo,
  IOauth2LoggingDto,
  IOauth2RefreshingDto,
  ISessionCreatingDto,
  ISessionVo,
  IThirdPartyTypeVo,
} from '@/api/dto/session';
import { restProxy } from '@/api/index';
import { AssociationTypeEnum } from '@/api/dto/enumeration';

export const baseUrl = '/auth';
export const oAuthUrl = '/auth/oauth2';

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
    url: `${oAuthUrl}/third_party_type`,
    method: 'get',
  });
}

/**
 * oauth获取授权url
 */
export function fetchAuthUrl(dto: IAuthorizationUrlGettingDto) {
  return restProxy<IAuthorizationUrlVo>({
    url: `${oAuthUrl}/url`,
    method: 'get',
    payload: dto,
  });
}

/**
 * oauth 三方登录
 * @param associationType
 * @param dto
 */
export function authLogin(associationType: AssociationTypeEnum | undefined, dto: IOauth2LoggingDto) {
  let url = `${oAuthUrl}/login`;
  if (associationType) {
    url += `/${associationType.toLowerCase()}`;
  }
  return restProxy<ISessionVo>({
    url,
    method: 'post',
    payload: dto,
  });
}

/**
 * oauth 刷新token
 * @param associationType
 * @param dto
 */
export function oauthRefreshToken(associationType: AssociationTypeEnum, dto: IOauth2RefreshingDto) {
  return restProxy<ISessionVo>({
    url: `${oAuthUrl}refresh/${associationType.toLowerCase()}`,
    method: 'put',
    payload: dto,
    auth: true,
  });
}

/**
 * oauth静默登录
 * @param code
 */
export function oauthSilentLogin(code: string) {
  return restProxy<ISessionVo>({
    url: `${oAuthUrl}/login/silent?code=${encodeURIComponent(code)}`,
    method: 'get',
  });
}
