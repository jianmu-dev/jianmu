import { AssociationTypeEnum } from '@/api/dto/enumeration';
import { IAssociationData } from '@/model/modules/session';

/**
 * 创建会话dto
 */
export interface ISessionCreatingDto extends Readonly<{
  username: string;
  password: string;
}> {
}

/**
 * 会话值对象
 */
export interface ISessionVo extends Readonly<{
  type: string;
  message?: string;
  token: string;
  id: number;
  username: string;
  avatarUrl: string;
  // associationId?: string;
  // associationType?: string;
  thirdPartyType: string;
  entryUrl?: string;
  associationData: IAssociationData;
}> {
}

/**
 * 获取授权url dto
 */
export interface IAuthorizationUrlGettingDto extends Readonly<{
  thirdPartyType: string,
  redirectUri: string
}> {
}

/**
 * 获取授权url vo
 */
export interface IAuthorizationUrlVo extends Readonly<{
  authorizationUrl: string
}> {
}

/**
 * 获取三方登录方式 vo
 */
export interface IThirdPartyTypeVo extends Readonly<{
  thirdPartyType: string,
  associationType?: AssociationTypeEnum,
  authMode: boolean
}> {
}

/**
 * oauth登录dto
 */
export interface IOauth2LoggingDto extends Readonly<{
  code: string,
  thirdPartyType: string,
  redirectUri: string
}> {
}

export interface IGitRepoLoggingDto extends Readonly<IOauth2LoggingDto & {
  ref: string,
  owner: string
}> {
}


/**
 * oauth refresh session dto
 */
export interface IOauth2RefreshingDto extends Readonly<{}> {
}

export interface IGitRepoTokenRefreshingDto extends Readonly<IOauth2LoggingDto & {
  ref: string,
  owner: string
}> {
}
