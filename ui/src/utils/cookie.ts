import Cookie, { CookieAttributes } from 'js-cookie';
import { ISession } from '@/model/modules/session';
import { encode } from 'js-base64';

/**
 * 设置cookie
 * @param key cookie键
 * @param value cookie值
 */
export const setCookie = (key: string, value: string) => {
  const cookieAttributes: CookieAttributes = {
    // 设置cookie不过期
    expires: 365 * 100,
  };
  // cookie domain 动态指定
  const domain = import.meta.env.VITE_COOKIE_DOMAIN;
  if (domain) {
    Object.assign(cookieAttributes, { domain });
  }
  // 注入cookie
  Cookie.set(key, value, cookieAttributes);
};

/**
 * 删除cookie
 * @param key
 */
export const deleteCookie = (key: string) => {
  const cookieAttributes: CookieAttributes = {
    // 设置cookie不过期
    expires: 365 * 100,
  };
  // cookie domain 动态指定
  const domain = import.meta.env.VITE_COOKIE_DOMAIN;
  if (domain) {
    Object.assign(cookieAttributes, { domain });
  }
  // 注入cookie
  Cookie.remove(key, cookieAttributes);
};

/**
 * 获取cookie
 * @param key
 */
export const getCookie = (key: string): string | undefined => {
  // 获取cookie
  return Cookie.get(key);
};

/**
 * 设置验证cookie
 */

export const setAuthCookie = (session: ISession) => {
  setCookie('JM-Session', encode(JSON.stringify(session)));
  setCookie('Authorization', session.sessionId);
};
