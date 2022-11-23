import type { App } from 'vue';
import fundebug from 'fundebug-javascript';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import FundebugVue from 'fundebug-vue';
// 开启FunDebug录屏功能
import 'fundebug-revideo';

export function useFunDebug(app: App) {
  fundebug.init({
    // 关联项目（测试）
    apikey: '2fcf7e79844d012817d304849e997227deeba333b8383a173edbd14fd3a3ab53',
    // silentHttp: true,
  });
  app.use(new FundebugVue(fundebug));
}
