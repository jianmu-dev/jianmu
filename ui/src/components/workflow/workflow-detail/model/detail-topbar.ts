import { executeImmediately } from "@/api/project";

type TriggerCallbackFnType = (error?: Error)=>void;

export class DeatilTopbar {
  private readonly proxy: any;
  private readonly projectId: string;
  private readonly triggerCallbackFn: TriggerCallbackFnType;
  constructor(proxy: any, projectId: string, triggerCallbackFn: TriggerCallbackFnType) {
    this.proxy = proxy;
    this.projectId = projectId;
    this.triggerCallbackFn = triggerCallbackFn;
  }
  /**
   * 触发新流程
   */
  async trigger():Promise<void> {
    try {
      console.log('触发新流程', this.projectId);
      await executeImmediately(this.projectId);
      this.triggerCallbackFn();
    } catch (error) {
      console.log('触发新流程 error');
      this.triggerCallbackFn(error as Error);
    }
  }
}