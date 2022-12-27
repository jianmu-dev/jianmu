import { ActionContext, createStore } from 'vuex';
import { IRootState, IScrollOffset } from '@/model';
import { IVersionVo } from '@/api/dto/common';
import { fetchVersion } from '@/api/view-no-auth';
import { RouteLocationNormalized } from 'vue-router';
import { IThirdPartyTypeVo } from '@/api/dto/session';
import { fetchThirdPartyType } from '@/api/session';

const store = createStore<IRootState>({
  // 开发环境开启严格模式，在严格模式下，无论何时发生了状态变更且不是由 mutation 函数引起的，将会抛出错误
  strict: process.env.NODE_ENV !== 'production',
  // 根状态
  state: {
    versions: [],
    thirdPartyType: '',
    authMode: 'readonly',
    workerTypes: [],
    parameterTypes: [],
    fromRoute: {
      path: '/',
      fullPath: '/',
    },
    scrollbarOffset: {},
  },
  // 根mutation
  mutations: {
    mutateVersions(state: IRootState, payload: IVersionVo[]): void {
      state.versions = payload;
    },
    mutateThirdPartyType(state: IRootState, payload: IThirdPartyTypeVo): void {
      state.thirdPartyType = payload.thirdPartyType;
      state.authMode = payload.authMode;
    },
    mutateWorkerTypes(state: IRootState, payload: string[]): void {
      state.workerTypes = payload;
    },

    mutateParameterTypes(state: IRootState, payload: string[]): void {
      state.parameterTypes = payload;
    },

    mutateFromRoute(
      state: IRootState,
      {
        to,
        from,
      }: {
        to: RouteLocationNormalized;
        from: RouteLocationNormalized;
      },
    ): void {
      if (to.path === from.path) {
        // 忽略重复
        return;
      }

      if (from.path === '/login') {
        // 过滤来源
        state.fromRoute = { path: '/', fullPath: '/' };
        return;
      }

      const { path, fullPath } = from;
      state.fromRoute = { path, fullPath };
    },
    mutateScrollbarOffset(
      state: IRootState,
      {
        fullPath,
        left,
        top,
      }: {
        fullPath: string;
      } & IScrollOffset,
    ) {
      const { scrollbarOffset } = state;
      scrollbarOffset[fullPath] = { left, top };
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

      // 初始化平台登录方式
      try {
        commit('mutateThirdPartyType', await fetchThirdPartyType());
      } catch (err) {
        console.log('fetch third party type failed', err.message);
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
  store.registerModule(namespace, module),
);

export default store;
