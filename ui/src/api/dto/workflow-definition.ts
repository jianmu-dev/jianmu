import { ProjectImporterTypeEnum } from '@/api/dto/enumeration';

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
