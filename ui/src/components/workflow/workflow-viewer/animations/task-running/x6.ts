import { BaseTaskRunning } from '../base-task-running';
import { Node } from '@antv/x6';

/**
 * X6任务执行中动画
 */
export default class X6TaskRunning extends BaseTaskRunning {
  private readonly node: Node;

  constructor(node: Node) {
    super();

    this.node = node;
  }

  start(): void {
    console.log(`${this.node.id} started`);
  }

  stop(): void {
    console.log(`${this.node.id} stopped`);
  }
}