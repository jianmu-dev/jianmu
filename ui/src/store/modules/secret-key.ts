import { ActionContext, Module } from 'vuex';
import { IRootState } from '@/model';
import { IState } from '@/model/modules/secret-key';
import { listNamespace } from '@/api/view-no-auth';
import { INamespacesVo } from '@/api/dto/secret-key';
import { CredentialManagerTypeEnum } from '@/api/dto/enumeration';

/**
 * 命名空间
 */
export const namespace = 'secret-key';

export default {
  namespaced: true,
  state: () => {
    return {
      credentialManagerType: CredentialManagerTypeEnum.LOCAL,
      namespaces: [],
    };
  },
  mutations: {
    mutateNamespaces(state: IState, { credentialManagerType, list }: INamespacesVo) {
      state.credentialManagerType = credentialManagerType;
      state.namespaces = list;
    },
    mutateNamespaceDeletion(state: IState, name: string) {
      const index = state.namespaces.findIndex(item => item.name === name);
      state.namespaces.splice(index, 1);
    },
  },
  actions: {
    async listNamespace({ commit }: ActionContext<IState, IRootState>): Promise<void> {
      commit('mutateNamespaces', await listNamespace());
    },
  },
} as Module<IState, IRootState>;