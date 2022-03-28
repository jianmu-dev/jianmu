import { IState } from '@/model/modules/project';
import { Module } from 'vuex';
import { IRootState } from '@/model';
import { SORT_TYPE_ENUM } from '@/api/dto/enumeration';
import { getStorage, setStorage } from '@/utils/storage';

/**
 * 命名空间
 */
export const namespace = 'project';
const DEFAULT_STATE: IState = { sortType: SORT_TYPE_ENUM.DEFAULT_SORT };

// module的state
function getState(): IState {
  return getStorage<IState>(namespace) || DEFAULT_STATE;
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
      setStorage(namespace, { sortType: payload });
    },
  },
} as Module<IState, IRootState>;
