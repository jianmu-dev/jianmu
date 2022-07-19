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
      title: '新增项目',
    },
    props: ({ query: { branch } }: RouteLocationNormalizedLoaded) => ({ branch }),
  },
  {
    name: 'update-project',
    path: 'project/editor/:id',
    component: () => import('@/views/project/editor.vue'),
    props: ({ params: { id } }: RouteLocationNormalizedLoaded) => ({ id }),
    meta: {
      title: '编辑项目',
    },
  },
] as RouteRecordRaw[];
