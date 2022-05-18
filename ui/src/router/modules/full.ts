import { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';

export default [
  {
    name: 'create-pipeline',
    path: 'project/pipeline-editor',
    component: () => import('@/views/project/pipeline-editor.vue'),
    meta: {
      title: '创建pipeline项目',
    },
  },
  {
    name: 'update-pipeline',
    path: 'project/pipeline-editor/:id',
    component: () => import('@/views/project/pipeline-editor.vue'),
    meta: {
      title: '编辑pipeline项目',
    },
    props: ({ params: { id } }: RouteLocationNormalizedLoaded) => ({ id }),
  },
  {
    name: 'workflow-execution-record-detail',
    path: 'workflow-execution-record/detail',
    component: () => import('@/views/workflow-execution-record/detail.vue'),
    props: ({
      query: { projectId, workflowExecutionRecordId },
    }: RouteLocationNormalizedLoaded) => ({
      projectId,
      workflowExecutionRecordId,
    }),
    meta: {
      title: '执行记录',
    },
  },
] as RouteRecordRaw[];
