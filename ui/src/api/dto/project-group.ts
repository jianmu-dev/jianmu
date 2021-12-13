import { BaseVo } from '@/api/dto/common';
/**
 * 项目组vo
 */
export interface IProjectGroupVo
  extends Readonly<
    BaseVo & {
      id: string;
      name: string;
      description?: string;
      sort: number;
      projectCount: number;
      isDefaultGroup: boolean;
      isShow: boolean;
    }
  > {}

/**
 * 创建项目组dto
 */

export interface IProjectGroupCreatingDto
  extends Readonly<{
    name: string;
    isShow: boolean;
    description?: string;
  }> {}
/**
 * 编辑项目组dto
 */

export interface IProjectGroupEditingDto
  extends Readonly<{
    name: string;
    isShow: boolean;
    description?: string;
  }> {}
/**
 * 创建项目组dto
 */

export interface IProjectGroupDto
  extends Readonly<{
    name: string;
    description?: string;
  }> {}

/**
 *修改项目组排序dto
 */

export interface IProjectGroupSortUpdatingDto
  extends Readonly<{
    originGroupId: string;
    targetGroupId: string;
  }> {}

/**
 * 项目组添加项目dto
 */
export interface IProjectGroupAddingDto
  extends Readonly<{
    projectGroupId: string;
    projectIds: string[];
  }> {}

/**
 * 修改项目组中的项目排序dto
 */
export interface IProjectSortUpdatingDto
  extends Readonly<{
    originProjectId: string;
    targetProjectId: string;
  }> {}
