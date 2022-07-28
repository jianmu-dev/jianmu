import { AxiosResponse } from 'axios';

/**
 * 超时错误
 */
export class TimeoutError extends Error {
  response: AxiosResponse;

  constructor(response: AxiosResponse, message?: string) {
    super(message);
    this.name = 'TimeoutError';
    this.response = response;
  }
}

/**
 * http错误
 */
export class HttpError extends Error {
  response: AxiosResponse;

  constructor(response: AxiosResponse, message?: string) {
    super(message);
    this.name = 'HttpError';
    this.response = response;
  }
}

/**
 * 重置内容错误
 */
export class ConflictError extends Error {
  response: AxiosResponse;

  constructor(response: AxiosResponse) {
    super();
    this.name = 'RestContentError';
    this.response = response;
  }
}
