import Cookie, { CookieAttributes } from 'js-cookie';

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
