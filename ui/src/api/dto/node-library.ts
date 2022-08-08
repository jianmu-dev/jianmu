import { OwnerTypeEnum, WorkerTypeEnum } from './enumeration';

export interface INodeCreatingDto extends Readonly<{
  name: string;
  description?: string;
  dsl: string;
}> {
}

export interface INodeVo extends Readonly<{
  icon: string;
  name: string;
  ownerName: string;
  ownerType: OwnerTypeEnum;
  ownerRef: string;
  creatorName: string;
  creatorRef: string;
  type: WorkerTypeEnum;
  description: string;
  ref: string;
  sourceLink: string;
  documentLink: string;
  versions: string[];
  deprecated: boolean;
}> {
}
