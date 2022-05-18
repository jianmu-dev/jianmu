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

/**
 * 本地/官方-获取节点定义版本列表
 */
export interface INodeDefVersionListVo
  extends Readonly<{
    versions: string[]
  }> {
}

/**
 * 获取节点定义版本-输入/输出参数
 */
export interface INodeParameterVo
  extends Readonly<{
    name: string;
    ref: string;
    type: string;
    description: string;
    value: object;
    required: boolean;
  }> {
}

/**
 * 本地-获取节点定义版本
 */
export interface INodeDefVersionVo
  extends Readonly<{
    ownerRef: string;
    ref: string;
    creatorName: string;
    creatorRef: string;
    version: string;
    description: string;
    resultFile: string;
    inputParameters: INodeParameterVo[];
    outputParameters: INodeParameterVo[];
    spec: string;
  }> {
}

/**
 * 官方-获取节点版本定义
 */
export interface INodeDefinitionVersionExampleVo
  extends Readonly<{
    id: number;
    versionNumber: string;
    description: string;
    workflowExample: string;
    pipelineExample: string;
    inputParams: INodeParameterVo[];
    outputParams: INodeParameterVo[];
    creatorName: string;
    creatorRef: string;
    creatorPortrait: string;
    createTime: string;
  }> {
}