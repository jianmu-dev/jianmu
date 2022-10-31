import { IGitRepoVo } from '@/api/dto/git-repo';

/**
 * 用户设置
 */
export interface IUserSettings {}

/**
 * 会话值对象
 */
export interface ISession
  extends Readonly<{
    clientType: string;
    // 过期时间
    expirationTime: number;
    sessionId: string;
    accountId: string;
    mobileBound: boolean;
    createdDate: string;
    lastModifiedDate: string;
    version: string;
    associationPlatform?: string;
    associationPlatformUserId?: string;
    associationId?: string;
    associationType?: string;
  }> {}

/**
 * vuex状态
 */
export interface IState {
  username: string;
  remember: boolean;
  userSettings: IUserSettings;
  session: ISession;
  gitRepo: IGitRepoVo;
}
