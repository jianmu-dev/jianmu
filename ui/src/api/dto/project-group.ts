/**
 * 查询项目组列表vo
 */
export interface IProjectGroupVo
  extends Readonly<{
    id: string;
    name: string;
    description?: string;
    sort: number;
    projectCount: number;
    createdTime: string;
    lastModifiedTime: string;
    isDefaultGroup: boolean;
  }> {}

/**
 * 创建项目组dto
 */

export interface IProjectGroupCreatingDto
  extends Readonly<{
    name: string;
    description?: string;
  }> {}
/**
 * 编辑项目组dto
 */

export interface IProjectGroupEditingDto
  extends Readonly<{
    name: string;
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
    originSort: number;
    targetSort: number;
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
    originSort: 0;
    targetSort: 0;
  }> {}
