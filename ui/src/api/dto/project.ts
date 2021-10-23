import { BaseVo } from '@/api/dto/common';
import { DslSourceEnum, DslTypeEnum, ProjectStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { INodeInfoVo } from '@/api/dto/workflow-execution-record';
import { IGitCloningDto } from '@/api/dto/workflow-definition';

/**
 * 保存项目dto
 */
export interface IProjectSavingDto extends Readonly<{
  id?: string;
  dslText: string;
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
  nodeDefs: INodeInfoVo[];
}> {
}

/**
 * 项目webhook vo
 */
export interface IProjectWebhookVo extends Readonly<{
  webhook: string;
}> {
}
