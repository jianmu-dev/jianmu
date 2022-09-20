/**
 * 事件
 */
export interface IEvent extends Readonly<{
  /**
   * 事件名
   */
  eventName: string;

  /**
   * 时间戳
   */
  timestamp: number;
}> {
}
