import { ActionContext, createStore } from 'vuex';
import { IRootState } from '@/model';
import { IVersionVo } from '@/api/dto/common';
import { fetchVersion } from '@/api/view-no-auth';

const store = createStore<IRootState>({
  // 开发环境开启严格模式，在严格模式下，无论何时发生了状态变更且不是由 mutation 函数引起的，将会抛出错误
  strict: process.env.NODE_ENV !== 'production',
  // 根状态
  state: {
    versions: [],
    workerTypes: [],
    parameterTypes: [],
  },
  // 根mutation
  mutations: {
    mutateVersions(state: IRootState, payload: IVersionVo[]): void {
      state.versions = payload;
    },

    mutateWorkerTypes(state: IRootState, payload: string[]): void {
      state.workerTypes = payload;
    },

    mutateParameterTypes(state: IRootState, payload: string[]): void {
      state.parameterTypes = payload;
    },

    mutateFromRouteFullPath(state: IRootState, payload: string): void {
      state.fromRouteFullPath = payload;
    },
  },
  // 根action
  actions: {
    async initialize({ state, commit }: ActionContext<IRootState, IRootState>): Promise<void> {
      if (state.versions.length === 0) {
        try {
          // 初始化版本
          commit('mutateVersions', await fetchVersion());
        } catch (err) {
          console.debug('fetch version failed', err.message);
        }
      }

      // if (state.workerTypes.length === 0) {
      //   // 初始化worker类型
      //   commit('mutateWorkerTypes', await listWorkerType());
      // }
      //
      // if (state.parameterTypes.length === 0) {
      //   // 初始化参数类型
      //   commit('mutateParameterTypes', await listParameterType());
      // }
    },
  },
});

// 动态加载模块
Object.values(import.meta.globEager('./modules/*.ts')).forEach(({ default: module, namespace }) =>
  store.registerModule(namespace, module));

export default store;