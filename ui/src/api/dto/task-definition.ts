import { BaseVo, IPageDto } from '@/api/dto/common';

/**
 * 任务参数值对象
 */
export interface ITaskParameterVo extends Readonly<{
  name: string;
  ref: string;
  type: string;
  description?: string;
  parameterId?: string;
  value: string;
}> {
}

/**
 * 挂载值对象
 */
export interface IMountVo extends Readonly<{
  type: 'BIND' | 'VOLUME' | 'TMPFS';
  source: string;
  target: string;
  readOnly: boolean;
}> {
}

/**
 * 主机配置值对象
 */
export interface IHostConfigVo extends Readonly<{
  mounts: IMountVo[];
}> {
}

/**
 * 容器规范值对象
 */
export interface IContainerSpecVo extends Readonly<{
  name?: string;
  hostName?: string;
  domainName?: string;
  user?: string;
  env?: string[];
  cmd?: string[];
  entrypoint?: string[];
  image: string;
  workingDir?: string;
  onBuild?: string[];
  networkDisabled?: boolean;
  stopSignal?: string;
  stopTimeout?: number;
  hostConfig?: IHostConfigVo;
  labels?: {
    [key: string]: string;
  };
  shell?: string[];
}> {
}

/**
 * 创建任务定义版本dto
 */
export interface ITaskDefinitionVersionCreatingDto extends Readonly<{
  version: string;
  description?: string;
  resultFile?: string;
  inputParameters?: ITaskParameterVo[];
  outputParameters?: ITaskParameterVo[];
  spec: IContainerSpecVo;
}> {
}

/**
 * 创建任务定义dto
 */
export interface ITaskDefinitionCreatingDto extends Readonly<ITaskDefinitionVersionCreatingDto & {
  name: string;
  ref: string;
  type: string;
}> {
}

/**
 * 查询任务定义dto
 */
export interface ITaskDefinitionQueryingDto extends Readonly<IPageDto & {
  name?: string;
}> {
}

/**
 * 任务定义值对象
 */
export interface ITaskDefinitionVo extends Readonly<BaseVo & {
  id: string;
  name: string;
  ref: string;
}> {
}

/**
 * 任务定义版本值对象
 */
export interface ITaskDefinitionVersionVo extends Readonly<BaseVo & {
  taskDefinitionId: string;
  name: string;
  taskDefinitionRef: string;
  definitionKey: string;
  description: string;
}> {
}

/**
 * 任务定义版本详情值对象
 */
export interface ITaskDefinitionVersionDetailVo extends ITaskDefinitionCreatingDto {
}