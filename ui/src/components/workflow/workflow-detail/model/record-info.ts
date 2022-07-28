import { terminate as _terminate } from '@/api/workflow-execution-record';

/**
 * RecordInfo类
 */
export class RecordInfo {
  private recordId: string;
  constructor(recordId: string) {
    this.recordId = recordId;
  }
  async terminate(recordId: string):Promise<void> {
    this.recordId = recordId;
    await _terminate(this.recordId);
  }
}

/**
 * 状态中文翻译
 */
export function statusTranslate(status: string) {
  const statusTranslate = {
    '': '未启动',
    INIT: '待启动',
    RUNNING: '执行中',
    FINISHED: '执行成功',
    TERMINATED: '执行失败',
    SUSPENDED: '已挂起',
  } as any;
  return statusTranslate[status];
}

