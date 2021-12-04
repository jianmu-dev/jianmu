import { BaseVo } from '@/api/dto/common';

/**
 * 项目组vo
 */
export interface IProjectGroupVo extends Readonly<BaseVo & {
  id: string;
  name: string;
  description?: string;
  sort: number;
  projectCount: number;
  isDefaultGroup: boolean;
}> {
}