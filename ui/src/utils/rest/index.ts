import axios, { AxiosResponse, AxiosTransformer, Method } from 'axios';
import qs from 'qs';
import { HttpError, TimeoutError } from '@/utils/rest/error';
import _store from '@/store';
import { namespace as sessionNs } from '@/store/modules/session';
import { IState as ISessionState } from '@/model/modules/session';

const instance = axios.create({
  // `baseURL` will be prepended to `url` unless `url` is absolute.
  // baseURL: 'http://xxx.xxx.xxx.xxx/',
  baseURL: import.meta.env.VITE_AXIOS_BASE_URL as string | undefined,
  // default is `0` (no timeout)
  // timeout: 10 * 1000,
});
instance.interceptors.request.use(
  config => {
    // 发送请求时记录当前的路由地址
    config.headers.locationHref = window.location.href;
    return config;
  },
  error => {
    return Promise.reject(error);
  },
);
type PayloadType = 'form-data' | 'json' | 'file';

export interface IRequest {
  url: string;
  method: Method;
  headers?: {
    [key: string]: string;
  };
  payload?: any;
  payloadType?: PayloadType;
  auth?: boolean;
  onDownloadProgress?: (event: any) => void;
  timeout?: number;
}

export default async function rest({
  url,
  method,
  headers = {},
  payload = {},
  payloadType = 'json',
  auth = false,
  onDownloadProgress,
  timeout,
}: IRequest): Promise<any> {
  const m = method.toLocaleLowerCase();
  let contentType: string | undefined;
  let transformRequest: AxiosTransformer | undefined;
  let params: object | undefined;
  let data: object | undefined;
  if (m === 'get' || m === 'delete' || m === 'head') {
    // eslint-disable-next-line prefer-const
    params = payload;
  } else if (m === 'post' || m === 'put' || m === 'patch') {
    switch (payloadType) {
      case 'form-data':
        contentType = 'application/x-www-form-urlencoded';
        // eslint-disable-next-line prefer-const
        transformRequest = data => qs.stringify(data, { indices: false });
        break;
      case 'file':
        contentType = 'multipart/form-data';
        break;
      default:
        contentType = 'application/json; charset=utf-8';
        break;
    }

    // eslint-disable-next-line prefer-const
    data = payload;
  } else {
    throw new Error(`Invalid supported method (value: ${method})`);
  }

  if (contentType) {
    headers['Content-Type'] = contentType;
  }

  const store = _store as any;
  const { session } = store.state[sessionNs] as ISessionState;

  if (session) {
    // 统一注入Authorization
    headers['Authorization'] = `${session.type} ${session.token}`;
  }

  let res;

  try {
    // eslint-disable-next-line prefer-const
    res = await instance.request({
      url,
      method: m,
      headers,
      params,
      paramsSerializer: (params: any) => qs.stringify(params, { indices: false }),
      data,
      transformRequest,
      onDownloadProgress,
      // 设置20秒超时
      timeout: onDownloadProgress ? 0 : timeout || 20 * 1000,
    });
  } catch (err) {
    if (err.message.startsWith('timeout of')) {
      throw new TimeoutError(err.message);
    }

    throw new HttpError(err.response, err.message);
  }

  const newToken = res.headers['x-authorization-token'];

  if (newToken) {
    // 更新认证
    store.commit(`${sessionNs}/mutateToken`, newToken);
  }

  if (m === 'head') {
    return res.headers;
  }

  const resContentType = res.headers['content-type'];

  if (resContentType && resContentType.includes('text/plain') && typeof res.data === 'object') {
    return res.request.responseText;
  }

  return res.data;
}

/**
 * 控制调用接口后，响应出错过程中是否存在页面路由变化
 * @param res 响应出错时的信息
 * @return 如果未发生变化返回true
 */
export function checkLocation(res: AxiosResponse): boolean {
  return window.location.href === res.config.headers.locationHref;
}
