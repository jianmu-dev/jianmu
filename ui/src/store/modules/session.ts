import { ActionContext, Module } from 'vuex';
import { ILoginForm, IState } from '@/model/modules/session';
import { IRootState } from '@/model';
import { authLogin, create, oauthRefreshToken } from '@/api/session';
import { IOauth2LoggingDto, IOauth2RefreshingDto, ISessionVo } from '@/api/dto/session';
import { getStorage, setStorage } from '@/utils/storage';

/**
 * 命名空间
 */
export const namespace = 'session';

const DEFAULT_STATE_KEY = '_default';

const DEFAULT_STATE: IState = {
  username: '',
  remember: false,
  userSettings: {},
  session: undefined,
};

/**
 * 获取状态
 * @param username
 */
function getState(username?: string): IState {
  let storage = getStorage<Record<string, IState>>(namespace);
  if (!storage) {
    storage = {
      [DEFAULT_STATE_KEY]: DEFAULT_STATE,
    };
  }
  return storage[username ? `${namespace}_${username}` : DEFAULT_STATE_KEY];
}

/**
 * 保存状态
 * @param state
 */
function saveState(state: IState): void {
  let storage = getStorage<Record<string, IState>>(namespace) as Record<string, IState>;
  if (!storage) {
    storage = {};
  }
  storage[DEFAULT_STATE_KEY] = state;
  // 备份，用于下次使用
  storage[`${namespace}_${state.username}`] = state;

  setStorage(namespace, storage);
}

export default {
  namespaced: true,
  state: () => {
    return getState();
  },
  mutations: {
    mutate(state: IState, { session, loginForm: { username, remember } }: {
      session: ISessionVo,
      loginForm: ILoginForm,
    }) {
      const previousState: IState = getState(username);

      state.username = username;
      state.remember = remember;
      if (previousState && previousState.userSettings) {
        // 恢复用户设置
        state.userSettings = previousState.userSettings;
      } else {
        // 数据被认为篡改场景
        // 默认用户设置
        state.userSettings = DEFAULT_STATE.userSettings;
      }
      state.session = session;
      saveState(state);
    },

    oauthMutate(state: IState, payload: ISessionVo) {
      state.session = payload;
      state.username = payload.username;
      // TODO 测试
      state.session.entryUrl = `/full/demo?owner=${payload.associationData.owner}&ref=${payload.associationData.ref}`;
      saveState(state);
    },

    mutateToken(state: IState, token: string) {
      (state.session as any).token = token;

      saveState(state);
    },

    mutateDeletion(state: IState) {
      // 直接将session重置，避免出现调用方法后session值还在的情况
      state.session = undefined;
      saveState(state);
    },
  },
  actions: {
    async create({ commit, state }: ActionContext<IState, IRootState>, loginForm: ILoginForm): Promise<void> {
      const session = await create({
        username: loginForm.username,
        password: loginForm.password,
      });

      commit('mutate', { session, loginForm });
    },
    async oauthLogin({
      commit,
      rootState,
    }: ActionContext<IState, IRootState>, payload: IOauth2LoggingDto): Promise<void> {
      const session = await authLogin(rootState.associationType!, payload);
      commit('oauthMutate', session);
    },
    async oauthRefresh({
      commit,
      rootState,
    }: ActionContext<IState, IRootState>, payload: IOauth2RefreshingDto): Promise<void> {
      const session = await oauthRefreshToken(rootState.associationType!, payload);
      commit('oauthMutate', session);
    },
  },
} as Module<IState, IRootState>;
