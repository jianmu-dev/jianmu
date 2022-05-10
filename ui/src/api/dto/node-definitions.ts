import { OwnerTypeEnum, VisibleTypeEnum } from './enumeration';

/**
 *  查看公开节点定义dto
 */
export interface INodeDefinitionSearchingDto
  extends Readonly<{
    pageNum: number;
    pageSize: number;
    accountId?: string;
    name?: string;
    type?: string;
  }> {
}

/**
 * 节点定义vo
 */
export interface INodeDefinitionVo
  extends Readonly<{
    id: number;
    ownerId: number;
    creatorName: string;
    ref: string;
    icon?: string;
    name: string;
    creatorRef?: string;
    ownerName: string;
    ownerType: OwnerTypeEnum;
    ownerRef: string;
    description?: string;
    visibleType: VisibleTypeEnum;
    lastModifyTime: string;
    ownerPortrait: string;
    downloadCount: string;
    deprecated: boolean;
  }> {
}
