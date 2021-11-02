import { ActionContext, Module } from 'vuex';
import { IRootState } from '@/model';
import { IState } from '@/model/modules/secret-key';
import { listNamespace } from '@/api/view-no-auth';
import { IPageVo } from '@/api/dto/common';
import { INamespaceVo } from '@/api/dto/secret-key';

/**
 * 命名空间
 */
export const namespace = 'secret-key';

export default {
  namespaced: true,
  state: () => {
    return {
      totalPages: 0,
      totalElements: 0,
      namespaces: [],
    };
  },
  mutations: {
    mutateNamespaces(state: IState, { total, pages, list }: IPageVo<INamespaceVo>) {
      state.totalElements = total;
      state.totalPages = pages;
      state.namespaces = list;
    },
    mutateNamespaceDeletion(state: IState, name: string) {
      const index = state.namespaces.findIndex(item => item.name === name);
      state.namespaces.splice(index, 1);
    },
  },
  actions: {
    async listNamespace({ commit }: ActionContext<IState, IRootState>): Promise<void> {
      const page = await listNamespace();
      commit('mutateNamespaces', page);
    },
  },
} as Module<IState, IRootState>;