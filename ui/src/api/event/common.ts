/**
 * 事件
 */
export interface IEvent extends Readonly<{
   /**
    * id
    */
   id: string;

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
