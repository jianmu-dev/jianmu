import { BaseVo, IPageDto } from '@/api/dto/common';
import {
  DslSourceEnum,
  DslTypeEnum,
  NodeTypeEnum,
  ProjectImporterTypeEnum,
  ProjectStatusEnum,
  SortTypeEnum,
  TriggerTypeEnum,
} from '@/api/dto/enumeration';

/**
 * 保存项目dto
 */
export interface IProjectSavingDto
  extends Readonly<{
    id?: string;
    dslText: string;
    projectGroupId: string;
  }> {}

/**
 * 克隆Git库dto
 */
export interface IGitCloningDto
  extends Readonly<{
    uri: string;
    projectGroupId: string;
    credential: {
      type?: ProjectImporterTypeEnum;
      namespace?: string;
      userKey?: string;
      passKey?: string;
      privateKey?: string;
    };
    branch: string;
  }> {}

/**
 * git值对象
 */
export interface IGitVo
  extends Readonly<{
    id: string;
    uri: string;
    branch: string;
  }> {}

/**
 * 导入项目dto
 */
export interface IProjectImportingDto
  extends Readonly<
    IGitCloningDto & {
      id: string;
      dslPath: string;
    }
  > {}

/**
 * 查询项目dto
 */
export interface IProjectQueryingDto
  extends Readonly<
    IPageDto & {
      projectGroupId?: string;
      name?: string;
      sortType?: SortTypeEnum;
    }
  > {}

/**
 * 项目id vo
 */
export interface IProjectIdVo
  extends Readonly<{
    id: string;
  }> {}

/**
 * 项目vo
 */
export interface IProjectVo
  extends Readonly<
    BaseVo & {
      id: string;
      name: string;
      source: DslSourceEnum;
      dslType: DslTypeEnum;
      gitRepoId?: string;
      startTime?: string;
      suspendedTime?: string;
      latestTime?: string;
      nextTime?: string;
      status: ProjectStatusEnum;
      eventBridgeId?: string;
      triggerType: TriggerTypeEnum;
      enabled: boolean;
      mutable: boolean;
      description?: string;
      concurrent: boolean | number;
      workflowInstanceId: string;
      serialNo: number;
      occurredTime: string;
      workflowRef: string;
      caches?: string[];
    }
  > {}

/**
 * 项目详情vo
 */
export interface IProjectDetailVo
  extends Readonly<
    BaseVo & {
      id: string;
      dslSource: DslSourceEnum;
      dslType: DslTypeEnum;
      gitRepoId?: string;
      workflowName: string;
      workflowRef: string;
      workflowVersion: string;
      steps: number;
      dslText: string;
      eventBridgeId?: string;
      triggerType: TriggerTypeEnum;
      projectGroupId: string;
      projectGroupName: string;
    }
  > {}

/**
 * 项目webhook vo
 */
export interface IProjectWebhookVo
  extends Readonly<{
    webhook: string;
  }> {}

/**
 * 流程模板vo
 */
export interface IProcessTemplateVo
  extends Readonly<{
    id: number;
    name: string;
    type: string;
    dsl: string;
    nodeDefs: [
      {
        name: string;
        description: string;
        type: string;
        icon: string;
        ownerRef: string;
        sourceLink: string;
        documentLink: string;
        workType: string;
      },
    ];
  }> {}

/**
 * 任务参数vo
 */
export interface ITaskParameterVo
  extends Readonly<{
    ref: string;
    expression: string;
  }> {}

/**
 * 流程节点vo
 */
export interface IWorkflowNodeVo
  extends Readonly<{
    /**
     * 节点定义名称
     */
    name: string;
    /**
     * 节点定义描述
     */
    description?: string;
    /**
     * 节点定义
     */
    metadata?: string;
    ref: string;
    type: string;
    taskParameters: ITaskParameterVo[];
    sources: string[];
    targets: string[];
  }> {}

/**
 * 全局参数vo
 */
export interface IGlobalParameterVo
  extends Readonly<{
    name: string;
    type: string;
    value: string | number | boolean;
  }> {}

/**
 * 流程vo
 */
export interface IWorkflowVo
  extends Readonly<{
    name: string;
    ref: string;
    type: DslTypeEnum;
    description?: string;
    version: string;
    nodes: IWorkflowNodeVo[];
    globalParameters: IGlobalParameterVo[];
    dslText: string;
  }> {}

/**
 * 节点定义vo
 */
export interface INodeDefVo
  extends Readonly<{
    name: string;
    description?: string;
    icon?: string;
    ownerName: string;
    ownerType: string;
    ownerRef: string;
    creatorName: string;
    creatorRef: string;
    sourceLink?: string;
    documentLink?: string;
    type: string;
    workerType: NodeTypeEnum;
  }> {}
