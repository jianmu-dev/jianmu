import { IVersionVo } from '@/api/dto/common';

/**
 * vuex根状态
 */
export interface IRootState {
  versions: IVersionVo[];
  workerTypes: string[];
  parameterTypes: string[];
  fromRoute: {
    path: string;
    fullPath: string;
  };
}