import { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';

export default [
  {
    name: 'browser-version-error',
    path: 'browser-version',
    component: () => import('@/views/error/browser-version.vue'),
    meta: {
      title: '浏览器版本错误',
    },
  },
  {
    name: 'http-status-error',
    path: 'http-status/:value',
    component: () => import('@/views/error/http-status.vue'),
    props: ({ params: { value }, query: { errMessage } }: RouteLocationNormalizedLoaded) => ({ value, errMessage }),
    meta: {
      title: '请求错误',
    },
  },
  {
    name: 'network-error',
    path: 'network',
    component: () => import('@/views/error/network.vue'),
    meta: {
      title: '网络异常',
    },
  },
] as RouteRecordRaw[];
