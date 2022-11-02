import { useStore } from 'vuex';
import { namespace } from '@/store/modules/session';

/**
 * 跳转到entryUrl
 */
export const toEntry = (): void => {
  const store = useStore();
  window.top.location.href = store.getters[`${namespace}/entryUrl`];
};

/**
 * 返回登录页
 */
export const toLogin = (): void => {
  const store = useStore();
  const entryUrl = store.getters[`${namespace}/entryUrl`];
  const currentLocationUrl = window.location.href;
  // 地址为hub登录地址时，带上重定向地址
  if (entryUrl === import.meta.env.VITE_JIANMUHUB_LOGIN_URL) {
    window.top.location.href = `${entryUrl}?redirectUrl=${encodeURIComponent(currentLocationUrl)}`;
    return;
  }
  toEntry();
};
