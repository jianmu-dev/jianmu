import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import _store from '@/store';
import { INDEX } from '@/router/path-def';
import { namespace as sessionNs } from '@/store/modules/session';
import { IState as ISessionState } from '@/model/modules/session';

/**
 * 加载业务模块路由
 * @param path
 * @param title
 * @param auth
 * @param layout
 * @param record
 * @param alias
 */
const loadModuleRoute = (
  path: string,
  title: string | undefined,
  auth: boolean,
  layout: Promise<any>,
  record: Record<string, { [key: string]: any }>,
  alias?: string,
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
      alias,
    },
  } as RouteRecordRaw;
};

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // platform模块
    loadModuleRoute(
      INDEX,
      '首页',
      false,
      import('@/layout/integration.vue'),
      import.meta.globEager('./modules/integration.ts'),
      'integration',
    ),
    // loadModuleRoute(INDEX, '首页', false, import('@/layout/platform.vue'), import.meta.globEager('./modules/platform.ts')),
    // full模块
    loadModuleRoute('/full', undefined, false, import('@/layout/full.vue'), import.meta.globEager('./modules/full.ts')),
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
router.beforeEach((to, from, next) => {
  const lastMatched = to.matched[to.matched.length - 1];

  if (lastMatched.meta.title) {
    document.title = `建木 - ${lastMatched.meta.title}`;
  } else {
    document.title = '建木';
  }

  const store = _store as any;
  store.commit('mutateFromRoute', { to, from });
  const entry = true;
  const { session } = store.state[sessionNs] as ISessionState;
  const entryUrl = store.getters[`${sessionNs}/entryUrl`];
  if (
    entry &&
    // 表示integration布局
    to.matched.find(({ meta: { alias } }) => alias === 'integration') &&
    // 表示非子页面
    window.top === window &&
    session
  ) {
    // 强制跳转到entryUrl
    window.top.location.href = entryUrl;
    return;
  }
  if (to.name === 'index' && !session && entry) {
    next({ name: 'http-status-error', params: { value: 404 } });
    return;
  }
  for (const m of to.matched) {
    if (m.meta.auth && !session) {
      // 处理认证
      next(false);
      return;
    }
  }

  next();
});
export default router;
