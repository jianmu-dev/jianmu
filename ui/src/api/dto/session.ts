/**
 * 创建会话dto
 */
export interface ISessionCreatingDto
  extends Readonly<{
    username: string;
    password: string;
  }> {}

/**
 * 会话值对象
 */
export interface ISessionVo
  extends Readonly<{
    token: string;
    type: string;
    id: number;
    username: string;
    thirdPartyType: string;
    avatarUrl: string;
    gitRepo?: string;
    gitRepoOwner?: string;
    gitRepoId?: string;
  }> {}

/**
 * 获取授权url dto
 */
export interface IAuthorizationUrlGettingDto
  extends Readonly<{
    thirdPartyType: string;
    redirectUri: string;
  }> {}

/**
 * 获取授权url vo
 */
export interface IAuthorizationUrlVo
  extends Readonly<{
    authorizationUrl: string;
  }> {}

/**
 * 获取三方登录方式 vo
 */
export interface IThirdPartyTypeVo
  extends Readonly<{
    thirdPartyType: string;
    entry: boolean;
    authMode: string;
  }> {}

/**
 * oauth登录dto
 */
export interface IOauth2LoggingDto
  extends Readonly<{
    code: string;
    thirdPartyType: string;
    redirectUri: string;
    gitRepo?: string;
    gitRepoOwner?: string;
  }> {}
