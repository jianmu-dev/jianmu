/**
 * 项目节点缓存vo
 */

export interface IProjectNodeCacheVo {
  name: string;
  metadata: string;
  path: string;
}

/**
 * 项目缓存vo
 */
export interface IProjectCacheVo
  extends Readonly<{
    id: string;
    name: string;
    available: boolean;
    workerId?: string;
    nodeCaches: IProjectNodeCacheVo[];
  }> {}

/**
 * 节点缓存vo
 */
export interface INodeCacheVo
  extends Readonly<{
    name: string;
    available: boolean;
    path: string;
  }> {}

/**
 * task Cache Vo
 */
export interface ITaskCacheVo
  extends Readonly<{
    source: string;
    target: string;
  }> {}
