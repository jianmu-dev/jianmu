import { createRouter, createWebHistory, RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';
import _store from '@/store';
import { AUTHORIZE_INDEX, LOGIN_INDEX, PLATFORM_INDEX } from '@/router/path-def';
import { namespace as sessionNs } from '@/store/modules/session';
import { IState as ISessionState } from '@/model/modules/session';
import LoginVerify from '@/views/login/dialog.vue';
import { AppContext } from 'vue';

/**
 * 加载业务模块路由
 * @param path
 * @param title
 * @param auth
 * @param layout
 * @param record
 */
const loadModuleRoute = (
  path: string,
  title: string | undefined,
  auth: boolean,
  layout: Promise<any>,
  record: Record<string, { [key: string]: any }>,
) => {
  const children: RouteRecordRaw[] = [];
  // 加载业务模块中的所有路由
  Object.values(Object.values(record)[0]).forEach(_export => children.push(..._export));

  return {
    path,
    component: () => layout,
    children,
    meta: {
      title,
      auth,
    },
  } as RouteRecordRaw;
};
export default (appContext: AppContext) => {
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        // 静默登录
        name: 'authorize',
        path: AUTHORIZE_INDEX,
        component: () => import('@/views/login/page.vue'),
        props: ({ query: { gitRepo, gitRepoOwner, code, error_description } }: RouteLocationNormalizedLoaded) => ({
          gitRepo,
          gitRepoOwner,
          code,
          error_description,
        }),
      },
      {
        // 登录
        name: 'login',
        path: LOGIN_INDEX,
        component: () => import('@/views/login/page.vue'),
        props: ({ query: { gitRepo, gitRepoOwner, code, error_description } }: RouteLocationNormalizedLoaded) => ({
          gitRepo,
          gitRepoOwner,
          code,
          error_description,
        }),
      },
      // platform模块
      loadModuleRoute(
        PLATFORM_INDEX,
        '首页',
        false,
        import('@/layout/platform.vue'),
        import.meta.globEager('./modules/platform.ts'),
      ),
      // full模块
      loadModuleRoute(
        '/full',
        undefined,
        false,
        import('@/layout/full.vue'),
        import.meta.globEager('./modules/full.ts'),
      ),
      // error模块
      loadModuleRoute(
        '/error',
        undefined,
        false,
        import('@/layout/error.vue'),
        import.meta.globEager('./modules/error.ts'),
      ),
      {
        // 默认
        // path匹配规则：按照路由的定义顺序
        path: '/:catchAll(.*)',
        redirect: {
          name: 'http-status-error',
          params: {
            value: 404,
          },
        },
      },
    ],
  });
  router.beforeEach(async (to, from, next) => {
    const lastMatched = to.matched[to.matched.length - 1];

    if (lastMatched.meta.title) {
      document.title = `建木 - ${lastMatched.meta.title}`;
    } else {
      document.title = '建木';
    }

    const store = _store as any;
    store.commit('mutateFromRoute', { to, from });
    const { session } = store.state[sessionNs] as ISessionState;

    for (const m of to.matched) {
      if (m.meta.auth && !session) {
        // 处理认证
        next(false);

        if (from.matched.length === 0) {
          await router.push({
            name: 'login',
            query: {
              redirectUrl: to.path === LOGIN_INDEX ? undefined : to.fullPath,
            },
          });
        } else {
          // 登录弹框
          await _store.dispatch(`${sessionNs}/openAuthDialog`, { appContext, LoginVerify });
        }
        return;
      }
    }

    next();
  });
  return router;
};
