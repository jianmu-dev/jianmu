import { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';

export default [
  {
    name: 'create-pipeline',
    path: 'project/pipeline-editor',
    component: () => import('@/views/project/pipeline-editor.vue'),
    meta: {
      title: '新增图形项目',
    },
    props: ({ query: { branch } }: RouteLocationNormalizedLoaded) => ({ branch }),
  },
  {
    name: 'update-pipeline',
    path: 'project/pipeline-editor/:id',
    component: () => import('@/views/project/pipeline-editor.vue'),
    meta: {
      title: '编辑图形项目',
    },
    props: ({ params: { id } }: RouteLocationNormalizedLoaded) => ({ id }),
  },
  {
    name: 'workflow-execution-record-detail',
    path: 'workflow-execution-record/detail',
    component: () => import('@/views/workflow-execution-record/detail.vue'),
    props: ({ query: { projectId, viewMode, triggerId, redirectUrl } }: RouteLocationNormalizedLoaded) => ({
      projectId,
      viewMode,
      triggerId,
      redirectUrl,
    }),
    meta: {
      title: '执行记录',
    },
  },
  {
    path: 'example',
    component: () => import('@/views/project/example.vue'),
  },
] as RouteRecordRaw[];
