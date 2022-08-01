import { namespace } from '@/store/modules/session';
import _store from '@/store';

export function pushTop(url: string) {
  const store = _store as any;
  const session = store.state[namespace].session;
  if (session) {
    // 多tab之间刷新页面后localstorage中保存的session会变化
    // 因此在路由切换时将vuex中session数据重新保存，保证当前session不被覆盖。
    store.commit(`${namespace}/oauthMutate`, session);
  }
  window.top.location.href = url;
}
