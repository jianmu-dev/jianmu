import { IPageDto } from '@/api/dto/common';
export interface ICategories extends Readonly<{
    id: number
    name: string
    icon: string
  }> {
  }


export interface IWorkflowTemplateViewingDto extends Readonly<IPageDto & {
    name: string
    templateCategoryId:number
  }> {
  }

export interface IContent extends Readonly<{
    id: number
    name: string
    type: string
    dsl:string
  }> {
  }
export interface ITemplateList extends Readonly<{
  content: IContent[] | [],
  pageable: {
    sort: {
      empty: boolean,
      sorted: boolean,
      unsorted: boolean
    },
    offset: number,
    pageNumber: number,
    pageSize: number,
    paged: boolean,
    unpaged: boolean
  },
  totalElements: number,
  totalPages: number,
  last: boolean,
  size: number,
  number: number,
  sort: {
    empty: boolean,
    sorted: boolean,
    unsorted: boolean
  },
  numberOfElements: number,
  first: boolean,
  empty: boolean
  }> {
  }
