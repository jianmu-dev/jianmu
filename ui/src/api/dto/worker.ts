import { WorkerTypeEnum, WorkerStatusEnum } from '@/api/dto/enumeration';

/**
 * WorkerVo
 */
export interface IWorkerVo {
  id: string;
  name: string;
  tags: string;
  capacity: number;
  os: string;
  arch: string;
  type: WorkerTypeEnum;
  status: WorkerStatusEnum;
  createdTime: string;
}
