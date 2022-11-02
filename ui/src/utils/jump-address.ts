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
  if (!(_store.state as any)[namespace].session.associationPlatform) {
    pushTop(`${import.meta.env.VITE_JIANMUHUB_LOGIN_URL}?redirectUrl=${encodeURIComponent(window.location.href)}`);
    return;
  }
  toEntry();
};
