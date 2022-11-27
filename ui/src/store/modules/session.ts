import { Module } from 'vuex';
import { ISession, IState } from '@/model/modules/session';
import { IRootState } from '@/model';
import { getCookie } from '@/utils/cookie';
import { decode } from 'js-base64';
import { IGitRepo } from '@/model/modules/git-repo';
import { toLogin } from '@/utils/jump-address';
import _router from '@/router';

export const DEFAULT_SESSION = {
  clientType: '',
  expirationTime: 0,
  sessionId: '',
  accountId: '',
  mobileBound: false,
  createdDate: '',
  lastModifiedDate: '',
  version: '',
} as ISession;
/**
 * 命名空间
 */
export const namespace = 'session';

function getState(): IState {
  return {
    gitRepo: {
      owner: '',
      ref: '',
    },
    session: DEFAULT_SESSION,
  };
}

export default {
  namespaced: true,
  state: () => {
    return getState();
  },
  mutations: {
    mutateSession(state: IState) {
      const cookie = getCookie('JM-Session');
      if (!cookie) {
        toLogin();
        return;
      }
      try {
        state.session = JSON.parse(decode(cookie)) as ISession;
      } catch (err) {
        toLogin();
      }
    },

    mutateGitRepo(state: IState, payload: IGitRepo) {
      state.gitRepo = payload;
    },
  },
  getters: {
    entryUrl(state: IState): string {
      if (!state.session.associationPlatform) {
        return (
          (_router.currentRoute.value.query.redirectUrl as string) ||
          // TODO 待改成通用SaaS首页
          '/'
        );
      }

      let url = '';
      switch (state.session.associationPlatform) {
        case 'GITLINK':
          url =
            import.meta.env.MODE !== 'development'
              ? `${import.meta.env.VITE_GITLINK_BASE_URL}/${state.gitRepo.owner}/${state.gitRepo.ref}/devops`
              : `${import.meta.env.VITE_GITLINK_BASE_URL}/demo?owner=${state.gitRepo.owner}&ref=${
                state.gitRepo.ref
              }&userId=${state.session.associationPlatformUserId}`;
          break;
      }
      return url;
    },
  },
} as Module<IState, IRootState>;
