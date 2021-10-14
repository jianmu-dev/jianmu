import { ActionContext, Module } from 'vuex';
import { IRootState } from '@/model';
import { IQueryForm, IState } from '@/model/modules/workflow-execution-record';
import { query } from '@/api/workflow-execution-record';
import { IPageVo } from '@/api/dto/common';
import { INodeInfoVo, ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
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
      totalElements: {
        executing: 0,
        completed: 0,
      },
      executing: {
        total: 0,
        pages: 0,
        list: [],
      },
      completed: {
        total: 0,
        pages: 0,
        list: [],
      },
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
    mutationTotalElement(state: IState, { status, totalElement }: {
      status: WorkflowExecutionRecordStatusEnum;
      totalElement: number;
    }) {
      switch (status) {
        case WorkflowExecutionRecordStatusEnum.RUNNING:
          state.totalElements.executing = totalElement;
          break;
        case WorkflowExecutionRecordStatusEnum.FINISHED:
          state.totalElements.completed = totalElement;
          break;
      }
    },
    mutateRecords(state: IState, { payload, page }: {
      payload: IQueryForm,
      page: IPageVo<IWorkflowExecutionRecordVo>,
    }) {
      const noQuery = !payload.id && !payload.name && !payload.workflowVersion;

      switch (payload.status) {
        case WorkflowExecutionRecordStatusEnum.RUNNING:
          if (noQuery) {
            state.totalElements.executing = page.total;
          }
          state.executing = page;
          break;
        case WorkflowExecutionRecordStatusEnum.FINISHED:
          if (noQuery) {
            state.totalElements.completed = page.total;
          }
          state.completed = page;
          break;
      }
    },
    mutateRecordDetail(state: IState, { project, allRecords = [], record, recordDsl, taskRecords = [], nodeInfos = [] }: Partial<{
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
    async fetchTotalElement({ commit }: ActionContext<IState, IRootState>, status: WorkflowExecutionRecordStatusEnum): Promise<void> {
      const page = await query({
        id: '',
        name: '',
        workflowVersion: '',
        status,
        pageNum: 1,
        pageSize: 0,
      });
      commit('mutationTotalElement', { status, totalElement: page.total });
    },

    async query({ commit }: ActionContext<IState, IRootState>, payload: IQueryForm): Promise<void> {
      const page = await query(payload);
      commit('mutateRecords', { payload, page });
    },

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