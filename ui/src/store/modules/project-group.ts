import { IState, IProjectGroupFoldStatusMapping } from '@/model/modules/project-group';
import { Module } from 'vuex';
import { IRootState } from '@/model';

/**
 * 命名空间
 */
export const namespace = 'project-group';
const DEFAULT_MAPPING: IProjectGroupFoldStatusMapping = {};

function getStorage() {
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
    storage = DEFAULT_MAPPING;
  }

  return storage;
}

function getState(): IState {
  const storage = getStorage();
  return {
    projectGroupFoldStatusMapping: storage,
  };
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
      state.projectGroupFoldStatusMapping[id] = status;
      localStorage.setItem(namespace, JSON.stringify(state.projectGroupFoldStatusMapping));
    },
  },
} as Module<IState, IRootState>;
