import { ActionContext, Module } from 'vuex';
import { IRootState } from '@/model';
import { IState } from '@/model/modules/workflow-execution-record';
import { ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import {
  fetchProjectDetail,
  fetchWorkflow,
  listAsyncTaskInstance,
  listWorkflowExecutionRecord,
} from '@/api/view-no-auth';
import yaml from 'yaml';
import { INodeDefVo, IProjectDetailVo } from '@/api/dto/project';

/**
 * 命名空间
 */
export const namespace = 'workflow-execution-record';

export default {
  namespaced: true,
  state: () => {
    return {
      recordDetail: {
        project: undefined,
        navScrollLeft: 0,
        allRecords: [],
        record: undefined,
        recordDsl: undefined,
        taskRecords: [],
        nodeInfos: [],
      },
    };
  },
  mutations: {
    mutateRecordDetail(
      state: IState,
      {
        project,
        allRecords = [],
        record,
        recordDsl,
        taskRecords = [],
        nodeInfos = [],
      }: Partial<{
        project: IProjectDetailVo;
        allRecords: IWorkflowExecutionRecordVo[];
        record: IWorkflowExecutionRecordVo;
        recordDsl: string;
        taskRecords: ITaskExecutionRecordVo[];
        nodeInfos: INodeDefVo[];
      }>,
    ) {
      const { recordDetail } = state;
      recordDetail.project = project;
      recordDetail.allRecords = allRecords;
      recordDetail.record = record;
      recordDetail.recordDsl = recordDsl;
      recordDetail.taskRecords = taskRecords;
      recordDetail.nodeInfos = nodeInfos;
    },
    mutateRecordDetailNavScrollLeft(state: IState, navScrollLeft) {
      const { recordDetail } = state;
      recordDetail.navScrollLeft = navScrollLeft;
    },
  },
  actions: {
    async fetchDetail(
      { commit }: ActionContext<IState, IRootState>,
      {
        projectId,
        workflowExecutionRecordId,
      }: {
        projectId: string;
        workflowExecutionRecordId?: string;
      },
    ): Promise<void> {
      const project = await fetchProjectDetail(projectId);
      const allRecords = await listWorkflowExecutionRecord(project.workflowRef);
      let record =
        allRecords.length === 0
          ? undefined
          : workflowExecutionRecordId
            ? allRecords.find(item => item.id === workflowExecutionRecordId)
            : allRecords[0];
      const { dslText, nodes } = await fetchWorkflow(
        record ? record.workflowRef : project.workflowRef,
        record ? record.workflowVersion : project.workflowVersion,
      );
      const recordDsl = dslText;
      const nodeInfos = nodes.filter(({ metadata }) => metadata).map(({ metadata }) => JSON.parse(metadata as string));
      if (!record) {
        const dsl = yaml.parse(recordDsl);
        const description = dsl.workflow?.description || dsl.pipeline?.description;

        record = {
          id: '',
          serialNo: 0,
          name: project.workflowName,
          workflowRef: project.workflowRef,
          workflowVersion: project.workflowVersion,
          description,
          startTime: '',
          status: '',
          triggerId: '',
          triggerType: project.triggerType,
        };
      }
      const taskRecords = !record.triggerId
        ? []
        : (await listAsyncTaskInstance(record.triggerId)).map(instance => {
          return {
            instanceId: '',
            businessId: instance.id,
            nodeName: instance.asyncTaskRef,
            defKey: instance.asyncTaskType,
            startTime: instance.startTime,
            endTime: instance.endTime,
            status: instance.status,
            taskCaches: instance.taskCaches,
          };
        });
      commit('mutateRecordDetail', { project, allRecords, record, recordDsl, taskRecords, nodeInfos });
    },
  },
} as Module<IState, IRootState>;
