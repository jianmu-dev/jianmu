import { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';

export default [
  {
    name: 'index',
    path: '',
    component: () => import('@/views/integration-index.vue'),
    meta: {
      title: '首页',
    },
  },
  // 组件库路由
  {
    path: 'component-lib',
    component: () => import('@/views/component-lib/index.vue'),
    meta: {
      title: '组件库',
    },
  },
  {
    // 密钥管理路由
    name: 'secret-key',
    path: 'secret-key',
    component: () => import('@/views/secret-key/ns-manager.vue'),
    meta: {
      title: '密钥管理',
    },
    children: [
      {
        name: 'manage-secret-key',
        path: 'manager/:namespace',
        component: () => import('@/views/secret-key/sk-manager.vue'),
        props: ({ params: { namespace } }: RouteLocationNormalizedLoaded) => ({
          ns: namespace,
        }),
        meta: {
          title: '详情',
        },
      },
    ],
  },
  {
    name: 'create-project',
    path: 'project/editor',
    component: () => import('@/views/project/editor.vue'),
    meta: {
      title: '新增代码项目',
    },
    props: ({ query: { branch } }: RouteLocationNormalizedLoaded) => ({ branch }),
  },
  {
    name: 'update-project',
    path: 'project/editor/:id',
    component: () => import('@/views/project/editor.vue'),
    props: ({ params: { id } }: RouteLocationNormalizedLoaded) => ({ id }),
    meta: {
      title: '编辑代码项目',
    },
  },
  {
    name: 'ext-param',
    path: 'ext-param',
    component: () => import('@/views/ext-param/ext-param.vue'),
    meta: {
      title: '外部参数',
    },
  },
] as RouteRecordRaw[];
