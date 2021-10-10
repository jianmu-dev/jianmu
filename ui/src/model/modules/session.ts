import { Mutable } from '@/utils/lib';
import { ISessionCreatingDto, ISessionVo } from '@/api/dto/session';

/**
 * 用户设置
 */
export interface IUserSettings {
  /**
   * 每页个数
   */
  pageSize?: {
    /**
     * 场景
     */
    [key: string]: number;
  }

  /**
   * 是否折叠主菜单
   */
  mainMenuCollapsed?: boolean;
}

/**
 * vuex状态
 */
export interface IState {
  username: string;
  remember: boolean;
  userSettings: IUserSettings;
  session?: ISessionVo;
}

/**
 * 存储
 */
export interface IStorage {
  [key: string]: IState;
}

/**
 * 登录表单
 */
export interface ILoginForm extends Mutable<ISessionCreatingDto> {
  remember: boolean;
}