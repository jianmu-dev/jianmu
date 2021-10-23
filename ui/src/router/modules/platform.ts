import { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';

export default [
  // 首页
  {
    name: 'index',
    path: '',
    component: () => import('@/views/index.vue'),
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
  // 事件桥接器路由
  {
    name: 'event-bridge',
    path: 'event-bridge',
    component: () => import('@/views/event-bridge/manager.vue'),
    meta: {
      title: '事件桥接器',
    },
    children: [{
      name: 'event-bridge-detail',
      path: 'detail/:id',
      component: () => import('@/views/event-bridge/detail.vue'),
      props: ({ params: { id } }: RouteLocationNormalizedLoaded) => ({ id }),
      meta: {
        title: '详情',
      },
    }],
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
    // 密钥管理路由
    name: 'secret-key',
    path: 'secret-key',
    component: () => import('@/views/secret-key/ns-manager.vue'),
    meta: {
      title: '密钥管理',
    },
    children: [{
      name: 'manage-secret-key',
      path: 'manager/:namespace',
      component: () => import('@/views/secret-key/sk-manager.vue'),
      props: ({ params: { namespace } }: RouteLocationNormalizedLoaded) => ({ ns: namespace }),
      meta: {
        title: '详情',
      },
    }],
  },
  {
    name: 'import-project',
    path: 'project/importer',
    component: () => import('@/views/project/importer.vue'),
    meta: {
      title: '导入项目',
    },
  }, {
    name: 'create-project',
    path: 'project/editor',
    component: () => import('@/views/project/editor.vue'),
    meta: {
      title: '新增项目',
    },
  }, {
    name: 'process-templates',
    path: 'process-templates',
    component: () => import('@/views/process-templates/manager.vue'),
    meta: {
      title: '流程模版',
    },
  }, {
    name: 'update-project',
    path: 'project/editor/:id',
    component: () => import('@/views/project/editor.vue'),
    props: ({ params: { id } }: RouteLocationNormalizedLoaded) => ({ id }),
    meta: {
      title: '编辑项目',
    },
  },
  {
    name: 'workflow-execution-record-detail',
    path: 'workflow-execution-record/detail',
    component: () => import('@/views/workflow-execution-record/detail.vue'),
    props: ({ query: { projectId, workflowExecutionRecordId } }: RouteLocationNormalizedLoaded) => ({
      projectId,
      workflowExecutionRecordId,
    }),
    meta: {
      title: '执行记录',
    },
  },
] as RouteRecordRaw[];