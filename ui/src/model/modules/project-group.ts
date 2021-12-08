import {
  IProjectGroupCreatingDto,
  IProjectGroupEditingDto,
} from '@/api/dto/project-group';
import { Mutable } from '@/utils/lib';
export interface IProjectGroupCreateFrom
  extends Mutable<IProjectGroupCreatingDto> {}
export interface IProjectGroupEditFrom
  extends Mutable<IProjectGroupEditingDto> {}
