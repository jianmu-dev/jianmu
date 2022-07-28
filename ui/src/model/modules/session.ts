import { Mutable } from '@/utils/lib';
import { ISessionCreatingDto, ISessionVo } from '@/api/dto/session';

/**
 * 用户设置
 */
export interface IUserSettings {
}

export interface IAssociationData {
  ref: string;
  owner: string;
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
