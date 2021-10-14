import { BaseVo, IPageDto } from '@/api/dto/common';
import { DslSourceEnum, DslTypeEnum, WorkflowDefinitionImporterTypeEnum } from '@/api/dto/enumeration';

/**
 * 查询流程定义dto
 */
export interface IWorkflowDefinitionQueryingDto extends Readonly<IPageDto & {
  name: string;
}> {
}

/**
 * 流程定义vo
 */
export interface IWorkflowDefinitionVo extends Readonly<BaseVo & {
  id: string;
  dslUrl: string;
  dslSource: DslSourceEnum;
  dslType: DslTypeEnum;
  gitRepoId?: string;
  workflowName: string;
  workflowRef: string;
  workflowVersion: string;
  steps: number;
  dslText: string;
}> {
}

/**
 * 创建流程定义dto
 */
export interface IWorkflowDefinitionSavingDto extends Readonly<{
  id?: string;
  dslText: string;
}> {
}

/**
 * 流程定义源码vo
 */
export interface IWorkflowDefinitionSourceVo extends Readonly<BaseVo & {
  projectId: string;
  workflowRef: string;
  workflowVersion: string;
  dslText: string;
}> {
}

/**
 * 克隆Git库dto
 */
export interface IGitCloningDto extends Readonly<{
  uri: string;
  credential: {
    type?: WorkflowDefinitionImporterTypeEnum;
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
 * 导入流程定义dto
 */
export interface IWorkflowDefinitionImportingDto extends Readonly<IGitCloningDto & {
  id: string;
  dslPath: string;
}> {
}
