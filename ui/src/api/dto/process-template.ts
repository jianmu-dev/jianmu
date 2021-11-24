import { IPageDto } from '@/api/dto/common';
import { DslTypeEnum, NodeTypeEnum } from './enumeration';

export interface ICategoriesVo extends Readonly<{
  id: number;
  name: string;
  icon?: string;
}> {
}

export interface IWorkflowTemplateViewingDto extends Readonly<IPageDto & {
  name?: string;
  templateCategoryId?: number;
}> {
}

export interface IContentVo extends Readonly<{
  id: number;
  name: string;
  type: DslTypeEnum;
  dsl: string;
  nodeDefs: {
    name: string;
    description?: string;
    type: string;
    icon?: string;
    ownerRef: string;
    sourceLink?: string;
    documentLink?: string;
    workerType: NodeTypeEnum;
  }[];
}> {
}

export interface ITemplateListVo extends Readonly<{
  totalElements: number;
  totalPages: number;
  pageNum: number;
  size: number;
  content: IContentVo[];
}> {
}
