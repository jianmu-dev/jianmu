import { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';

export default [
  // 首页
  {
    name: 'index',
    path: '',
    component: () => import('@/views/index.vue'),
    props: ({
      query: { searchName, projectGroupId },
    }: RouteLocationNormalizedLoaded) => ({
      searchName,
      projectGroupId,
    }),
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
  // 节点库路由
  {
    name: 'node-library',
    path: 'node-library',
    component: () => import('@/views/node-library/node-library-manager.vue'),
    meta: {
      title: '本地节点库',
    },
  },
  {
    name: 'project-group',
    path: 'project-group',
    component: () => import('@/views/project-group/project-group-manager.vue'),
    meta: {
      title: '分组管理',
    },
    children: [
      {
        name: 'project-group-detail',
        path: 'detail/:id',
        component: () =>
          import('@/views/project-group/project-group-detail.vue'),
        props: ({ params: { id } }: RouteLocationNormalizedLoaded) => ({ id }),
        meta: {
          title: '详情',
        },
      },
    ],
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
    name: 'import-project',
    path: 'project/importer',
    component: () => import('@/views/project/importer.vue'),
    meta: {
      title: '导入项目',
    },
  },
  {
    name: 'create-project',
    path: 'project/editor',
    component: () => import('@/views/project/editor.vue'),
    meta: {
      title: '新增项目',
    },
  },
  {
    name: 'process-template',
    path: 'process-template',
    component: () => import('@/views/process-template/manager.vue'),
    props: ({
      query: { processTemplatesName },
    }: RouteLocationNormalizedLoaded) => ({
      processTemplatesName,
    }),
    meta: {
      title: '流程模版',
      keepAlive: true,
    },
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
