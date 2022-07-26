import { IVersionVo } from '@/api/dto/common';
import { AssociationTypeEnum } from '@/api/dto/enumeration';

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
  entry: boolean;
  associationType?: AssociationTypeEnum;
  thirdPartyType: string;
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
