import {
  IProjectGroupAddingDto,
  IProjectGroupCreatingDto,
  IProjectGroupEditingDto,
} from '@/api/dto/project-group';
import { Mutable } from '@/utils/lib';

export interface IProjectGroupCreateFrom
  extends Mutable<IProjectGroupCreatingDto> {
}

export interface IProjectGroupEditFrom
  extends Mutable<IProjectGroupEditingDto> {
}

export interface IProjectGroupAddingForm
  extends Mutable<IProjectGroupAddingDto> {
}

export interface IProjectGroupFoldStatusMapping {
  [key: string]: boolean
}

export interface IState {
  projectGroupFoldStatusMapping: IProjectGroupFoldStatusMapping
}
