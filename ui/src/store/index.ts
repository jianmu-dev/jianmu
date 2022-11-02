import { ActionContext, createStore } from 'vuex';
import { IRootState, IScrollOffset } from '@/model';
import { IVersionVo } from '@/api/dto/common';
import { fetchVersion } from '@/api/view-no-auth';
import { RouteLocationNormalized } from 'vue-router';
import { ISession } from '@/model/modules/session';
import { namespace as sessionNs } from '@/store/modules/session';
import { getGitRepo } from '@/api/git-repo';
import { JM_ENTRY_URL } from '@/utils/constants';

const store = createStore<IRootState>({
  // 开发环境开启严格模式，在严格模式下，无论何时发生了状态变更且不是由 mutation 函数引起的，将会抛出错误
  strict: process.env.NODE_ENV !== 'production',
  // 根状态
  state: {
    versions: [],
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
      // 初始化session
      commit(`${sessionNs}/mutateSession`, undefined, { root: true });
      const session = (state as any)[`${sessionNs}`].session as ISession;
      if (session.associationType === 'GIT_REPO' && session.associationId) {
        // TODO 调用接口获取gitRepo
        const gitRepo = await getGitRepo(session.associationId);
        commit(`${sessionNs}/mutateGitRepo`, gitRepo, { root: true });
      }
      // 初始化来源
      if (!session.associationPlatform && document.referrer) {
        sessionStorage.setItem(JM_ENTRY_URL, document.referrer);
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
