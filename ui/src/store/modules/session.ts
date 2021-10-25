import { ActionContext, Module } from 'vuex';
import { ILoginForm, IState, IStorage } from '@/model/modules/session';
import { IRootState } from '@/model';
import { create } from '@/api/session';
import { ISessionVo } from '@/api/dto/session';

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
 * 获取存储
 */
function getStorage(): IStorage {
  let storage;

  const storageStr = localStorage.getItem(namespace);
  if (storageStr) {
    try {
      storage = JSON.parse(storageStr);
    } catch (err) {
      console.warn('Abnormal session storage, restore default');
    }
  }

  if (!storage) {
    storage = {
      [DEFAULT_STATE_KEY]: DEFAULT_STATE,
    };
  }

  return storage as IStorage;
}

/**
 * 获取状态
 * @param username
 */
function getState(username?: string): IState {
  const storage = getStorage();

  return storage[username ? `${namespace}_${username}` : DEFAULT_STATE_KEY];
}

/**
 * 保存状态
 * @param state
 */
function saveState(state: IState): void {
  const storage = getStorage();

  storage[DEFAULT_STATE_KEY] = state;
  // 备份，用于下次使用
  storage[`${namespace}_${state.username}`] = state;

  localStorage.setItem(namespace, JSON.stringify(storage));
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

    mutateToken(state: IState, token: string) {
      (state.session as any).token = token;

      saveState(state);
    },

    mutateDeletion(state: IState) {
      state.session = DEFAULT_STATE.session;

      saveState(state);
    },
  },
  actions: {
    async create({ commit }: ActionContext<IState, IRootState>, loginForm: ILoginForm): Promise<void> {
      const session = await create({
        username: loginForm.username,
        password: loginForm.password,
      });

      commit('mutate', { session, loginForm });
    },
  },
} as Module<IState, IRootState>;