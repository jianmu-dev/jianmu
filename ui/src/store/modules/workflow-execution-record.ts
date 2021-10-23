import { ActionContext, Module } from 'vuex';
import { IRootState } from '@/model';
import { IState } from '@/model/modules/workflow-execution-record';
import { INodeInfoVo, ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { fetchDsl, fetchProjectDetail, listTask, listWorkflowExecutionRecord } from '@/api/view-no-auth';
import yaml from 'yaml';
import { IProjectDetailVo } from '@/api/dto/project';

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
    mutateRecordDetail(state: IState, {
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
      nodeInfos: INodeInfoVo[];
    }>) {
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
    async fetchDetail({ commit }: ActionContext<IState, IRootState>, { projectId, workflowExecutionRecordId }: {
      projectId: string;
      workflowExecutionRecordId?: string;
    }): Promise<void> {
      const project = await fetchProjectDetail(projectId);
      const allRecords = await listWorkflowExecutionRecord(project.workflowRef);
      let record = allRecords.length === 0 ? undefined : (workflowExecutionRecordId ? allRecords.find(item => item.id === workflowExecutionRecordId) : allRecords[0]);
      const recordDsl = record ? await fetchDsl(record.workflowRef, record.workflowVersion) : project.dslText;
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
      const taskRecords = !record.id ? [] : await listTask(record.id);
      // 优先匹配快照
      const nodeInfos = taskRecords.map(taskRecord => taskRecord.nodeInfo);
      nodeInfos.push(...project.nodeDefs);
      commit('mutateRecordDetail', { project, allRecords, record, recordDsl, taskRecords, nodeInfos });
    },
  },
} as Module<IState, IRootState>;