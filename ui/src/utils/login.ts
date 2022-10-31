import { useStore } from 'vuex';
import { namespace } from '@/store/modules/session';

/**
 * 返回登录页
 */
export const toLogin = (): void => {
  const store = useStore();
  let entryUrl = store.getters[`${namespace}/entryUrl`];
  const currentLocationUrl = window.location.href;
  // 地址为hub登录地址时，带上重定向地址
  if (entryUrl === import.meta.env.VITE_JIANMUHUB_LOGIN_URL) {
    entryUrl += `?redirectUrl=${encodeURIComponent(currentLocationUrl)}`;
  }
  window.top.location.href = entryUrl;
};
