import { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';
import { globalT as t } from '@/utils/i18n';
export default [
  // 首页
  {
    name: 'index',
    path: '',
    component: () => import('@/views/index.vue'),
    props: ({ query: { searchName, projectGroupId } }: RouteLocationNormalizedLoaded) => ({
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
  // Worker管理路由
  {
    name: 'worker-manager',
    path: 'worker-manager',
    component: () => import('@/views/workers/workers-manager.vue'),
    meta: {
      title: t('breadcrumb.worker'),
    },
  },
  // 节点库路由
  {
    name: 'node-library',
    path: 'node-library',
    component: () => import('@/views/node-library/node-library-manager.vue'),
    meta: {
      title: t('breadcrumb.localNode'),
    },
  },
  {
    name: 'project-group',
    path: 'project-group',
    component: () => import('@/views/project-group/project-group-manager.vue'),
    meta: {
      title: t('breadcrumb.group'),
    },
    children: [
      {
        name: 'project-group-detail',
        path: 'detail/:id',
        component: () => import('@/views/project-group/project-group-detail.vue'),
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
      title: t('breadcrumb.secretKey'),
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
          title: t('breadcrumb.Details'),
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
      title: t('breadcrumb.addProject'),
    },
  },
  {
    name: 'process-template',
    path: 'process-template',
    component: () => import('@/views/process-template/manager.vue'),
    props: ({ query: { processTemplatesName } }: RouteLocationNormalizedLoaded) => ({
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
