import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';

/**
 * 排序任务列表
 * @param tasks
 * @param desc
 * @param nodeName
 */
export function sortTasks(tasks: ITaskExecutionRecordVo[], desc: boolean, nodeName?: string): ITaskExecutionRecordVo[] {
  if (nodeName) {
    tasks = tasks.filter(task => task.nodeName === nodeName);
  }

  // 按开始时间降序排序
  return tasks.sort((t1, t2) => {
    const st1 = Date.parse(t1.startTime);
    const st2 = Date.parse(t2.startTime);
    if (st1 === st2) {
      return 0;
    }
    if (st1 > st2) {
      return desc ? -1 : 1;
    }
    return desc ? 1 : -1;
  });
}