import _store from '@/store';
import { namespace } from '@/store/modules/session';

/**
 * 跳转指定地址
 * @param url
 */
export const pushTop = (url: string): void => {
  window.top.location.href = url;
};

/**
 * 跳转到entryUrl
 */
export const toEntry = (): void => {
  pushTop(_store.getters[`${namespace}/entryUrl`]);
};

/**
 * 返回登录页
 */
export const toLogin = (): void => {
  const entryUrl = _store.getters[`${namespace}/entryUrl`];
  const currentLocationUrl = window.location.href;
  // 地址为hub登录地址时，带上重定向地址
  if (entryUrl === import.meta.env.VITE_JIANMUHUB_LOGIN_URL) {
    pushTop(`${entryUrl}?redirectUrl=${encodeURIComponent(currentLocationUrl)}`);
    return;
  }
  toEntry();
};
