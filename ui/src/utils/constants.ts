/**
 * 起始页码
 */
export const START_PAGE_NUM = 1;

/**
 * 默认每页个数
 */
export const DEFAULT_PAGE_SIZE = 10;

/**
 * 默认日期格式
 */
export const DEFAULT_DATE_FORMAT = 'yyyy-mm-dd';

/**
 * 默认日期时间格式
 */
export const DEFAULT_DATETIME_FORMAT = 'yyyy-mm-dd HH:MM:ss';

/**
 * API前缀
 */
export const API_PREFIX =
  (import.meta.env.MODE === 'cdn' ? import.meta.env.VITE_JIANMUHUB_API_BASE_URL : '') + '/jianmu_saas';
