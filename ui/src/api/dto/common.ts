/**
 * 错误信息
 */
import { NodeTypeEnum } from '@/api/dto/enumeration';

export interface IErrorMessageVo extends Readonly<{
  statusCode: number;
  timestamp: Date;
  message: string;
  description: string;
}> {
}

/**
 * 抽象值对象
 */
export interface BaseVo extends Readonly<{
  /**
   * 创建时间
   */
  createdTime: string;

  /**
   * 最后修改人
   */
  lastModifiedBy: string;

  /**
   * 最后修改时间
   */
  lastModifiedTime: string;
}> {
}

/**
 * 分页dto
 */
export interface IPageDto extends Readonly<{
  /**
   * 页码
   */
  pageNum: number;

  /**
   * 每页个数
   */
  pageSize: number;

  /**
   * 分页类型
   */
  type?: NodeTypeEnum;

  /**
   * 节点名
   */
  name?: string;
}> {
}

/**
 * 分页值对象
 */
export interface IPageVo<T> extends Readonly<{
  /**
   * 总个数
   */
  total: number;

  /**
   * 总页数
   */
  pages: number;

  /**
   * 数据
   */
  list: T[];

  /**
   * 当前页码
   */
  pageNum: number;
}> {
}

/**
 * 版本
 */
export interface IVersionVo extends Readonly<{
  versionNo: string;
  releaseUrl: string;
}> {
}

/**
 * hub节点分页值对象
 */
export interface IHubNodePageVo<T>
  extends Readonly<{
    /**
     * 总个数
     */
    totalElements: number;
    /**
     * 当前页
     */
    pageNum: number;
    /**
     * 总页数
     */
    totalPages: number;
    /**
     * 当前加载数量
     */
    size: number;
    /**
     * 数据
     */
    content: T[];
  }> {
}
