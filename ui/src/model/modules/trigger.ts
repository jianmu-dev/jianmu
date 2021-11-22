import { Mutable } from '@/utils/lib';
import { IWebRequestVo } from '@/api/dto/trigger';
/**
 * webhook请求列表
 */
export interface IWebhookRequestTable extends Mutable<IWebRequestVo> {}
