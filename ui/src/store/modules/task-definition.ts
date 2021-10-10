import { ActionContext, Module } from 'vuex';
import { IRootState } from '@/model';
import { IDeleteVersionForm, IQueryForm, IState } from '@/model/modules/task-definition';
import { deleteVersion, listVersion, query } from '@/api/task-definition';
import { IPageVo } from '@/api/dto/common';
import { ITaskDefinitionVersionVo, ITaskDefinitionVo } from '@/api/dto/task-definition';

/**
 * 命名空间
 */
export const namespace = 'task-definition';

export default {
  namespaced: true,
  state: () => {
    return {
      totalPages: 0,
      totalElements: 0,
      definitions: [],
      versions: [],
    };
  },
  mutations: {
    mutateDefinitions(state: IState, { total, pages, list }: IPageVo<ITaskDefinitionVo>) {
      state.totalElements = total;
      state.totalPages = pages;
      state.definitions = list;
    },

    mutateVersions(state: IState, { taskDefRef, versions }: {
      taskDefRef: string;
      versions: ITaskDefinitionVersionVo[];
    }) {
      const index = state.definitions.findIndex(item => item.ref === taskDefRef);
      state.versions[index] = versions;
    },

    mutateVersionDeletion(state: IState, { taskDefRef, taskDefVersion }: IDeleteVersionForm) {
      const iIndex = state.definitions.findIndex(item => item.ref === taskDefRef);
      const jIndex = state.versions[iIndex].findIndex(item => item.name === taskDefVersion);

      state.versions[iIndex].splice(jIndex, 1);
    },
  },
  actions: {
    async query({ commit }: ActionContext<IState, IRootState>, payload: IQueryForm): Promise<void> {
      const page = await query(payload);
      commit('mutateDefinitions', page);
    },

    async listVersion({ commit }: ActionContext<IState, IRootState>, taskDefRef: string): Promise<void> {
      const versions = await listVersion(taskDefRef);
      commit('mutateVersions', { taskDefRef, versions });
    },

    async deleteVersion({ commit }: ActionContext<IState, IRootState>, {
      taskDefRef,
      taskDefVersion,
    }: IDeleteVersionForm): Promise<void> {
      await deleteVersion(taskDefRef, taskDefVersion);
      commit('mutateVersionDeletion', { taskDefRef, taskDefVersion });
    },
  },
} as Module<IState, IRootState>;