import { IState } from '@/model/modules/project-group';
import { Module } from 'vuex';
import { IRootState } from '@/model';
import { getStorage, setStorage } from '@/utils/storage';

/**
 * 命名空间
 */
export const namespace = 'project-group';
const DEFAULT_STATE: IState = {};

function getState(): IState {
  return getStorage<IState>(namespace) || DEFAULT_STATE;
}

export default {
  namespaced: true,
  state: () => {
    return getState();
  },
  mutations: {
    mutate(state: IState, payload: { id: string, status: boolean }) {
      const { id, status } = payload;
      // 根据项目组id存入对应的折叠状态
      state[id] = status;
      setStorage(namespace, state);
    },
  },
} as Module<IState, IRootState>;
