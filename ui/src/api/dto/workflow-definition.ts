import { ProjectImporterTypeEnum } from '@/api/dto/enumeration';

/**
 * 创建流程定义dto
 */
export interface IWorkflowDefinitionSavingDto extends Readonly<{
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
 * 导入流程定义dto
 */
export interface IWorkflowDefinitionImportingDto extends Readonly<IGitCloningDto & {
  id: string;
  dslPath: string;
}> {
}
