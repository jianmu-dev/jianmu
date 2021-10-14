import { ActionContext, Module } from 'vuex';
import { IRootState } from '@/model';
import { IQueryForm, IState } from '@/model/modules/workflow-definition';
import { query } from '@/api/workflow-definition';
import { IPageVo } from '@/api/dto/common';
import { IWorkflowDefinitionVo } from '@/api/dto/workflow-definition';

/**
 * 命名空间
 */
export const namespace = 'workflow-definition';

export default {
  namespaced: true,
  state: () => {
    return {
      totalPages: 0,
      totalElements: 0,
      definitions: [],
    };
  },
  mutations: {
    mutateDefinitions(state: IState, { total, pages, list }: IPageVo<IWorkflowDefinitionVo>) {
      state.totalElements = total;
      state.totalPages = pages;
      state.definitions = list;
    },
  },
  actions: {
    async query({ commit }: ActionContext<IState, IRootState>, payload: IQueryForm): Promise<void> {
      const page = await query(payload);
      commit('mutateDefinitions', page);
    },
  },
} as Module<IState, IRootState>;