import { IVersionVo } from '@/api/dto/common';

/**
 * 滚动偏移量
 */
export interface IScrollOffset {
  left: number;
  top: number;
}

/**
 * vuex根状态
 */
export interface IRootState {
  versions: IVersionVo[];
  thirdPartyType: string;
  authMode: string;
  workerTypes: string[];
  parameterTypes: string[];
  fromRoute: {
    path: string;
    fullPath: string;
  };
  scrollbarOffset: {
    [fullPath: string]: IScrollOffset;
  };
}
