import { IState } from '@/model/modules/project';
import { Module } from 'vuex';
import { IRootState } from '@/model';
import { SORT_TYPE_ENUM } from '@/api/dto/enumeration';

/**
 * 命名空间
 */
export const namespace = 'project';
const DEFAULT_STATE: IState = { sortType: SORT_TYPE_ENUM.DEFAULT_SORT };

// 获取保存在localStorage的值
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
    storage = DEFAULT_STATE;
  }

  return storage;
}

// module的state
function getState(): IState {
  const storage = getStorage();
  return {
    sortType: storage.sortType,
  };
}

export default {
  namespaced: true,
  state: () => {
    return getState();
  },
  mutations: {
    mutate(state: IState, payload: SORT_TYPE_ENUM) {
      // 改变项目组列表的排序方式
      state.sortType = payload;
      localStorage.setItem(namespace, JSON.stringify({ sortType: payload } as IState));
    },
  },
} as Module<IState, IRootState>;
