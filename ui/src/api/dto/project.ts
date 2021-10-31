import { BaseVo } from '@/api/dto/common';
import {
  DslSourceEnum,
  DslTypeEnum,
  ProjectImporterTypeEnum,
  ProjectStatusEnum,
  TriggerTypeEnum,
} from '@/api/dto/enumeration';

/**
 * 保存项目dto
 */
export interface IProjectSavingDto extends Readonly<{
  id?: string;
  dslText: string;
}> {
}


/**
 * 克隆Git库dto
 */
export interface IGitCloningDto extends Readonly<{
  uri: string;
  credential: {
    type?: ProjectImporterTypeEnum;
    namespace?: string;
    userKey?: string;
    passKey?: string;
    privateKey?: string
  };
  branch: string;
}> {
}

/**
 * git值对象
 */
export interface IGitVo extends Readonly<{
  id: string;
  uri: string;
  branch: string;
}> {
}

/**
 * 导入项目dto
 */
export interface IProjectImportingDto extends Readonly<IGitCloningDto & {
  id: string;
  dslPath: string;
}> {
}

/**
 * 查询项目dto
 */
export interface IProjectQueryingDto extends Readonly<{
  name?: string;
}> {
}

/**
 * 项目vo
 */
export interface IProjectVo extends Readonly<BaseVo & {
  id: string;
  name: string;
  source: DslSourceEnum;
  dslType: DslTypeEnum;
  gitRepoId?: string;
  latestTime?: string;
  nextTime?: string;
  status: ProjectStatusEnum;
  eventBridgeId?: string;
  triggerType: TriggerTypeEnum;
}> {
}

/**
 * 项目详情vo
 */
export interface IProjectDetailVo extends Readonly<BaseVo & {
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
}> {
}

/**
 * 项目webhook vo
 */
export interface IProjectWebhookVo extends Readonly<{
  webhook: string;
}> {
}

/**
 * 流程模板vo
 */
export interface IProcessTemplateVo extends Readonly<{
  id: number
  name: string
  type: string
  dsl: string
  nodeDefs: [
    {
      name: string
      description: string
      type: string
      icon: string
      ownerRef: string
      sourceLink: string
      documentLink: string
      workType: string
    }
  ]
}> {
}