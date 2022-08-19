import { namespace } from '@/store/modules/session';
import _store from '@/store';
import Security from '@/views/common/security.vue';
import dynamicRender from '@/utils/dynamic-render';
import { AppContext } from 'vue';

export async function pushTop(url: string, appContext: AppContext) {
  // @ts-ignore
  if (document.hasStorageAccess && !await document.hasStorageAccess()) {
    // 检验到浏览器为safari且开启网站跟踪，弹窗提示
    dynamicRender(Security, appContext);
    return;
  }
  const store = _store as any;
  const session = store.state[namespace].session;
  if (session) {
    // 多tab之间刷新页面后localstorage中保存的session会变化
    // 因此在路由切换时将vuex中session数据重新保存，保证当前session不被覆盖。
    store.commit(`${namespace}/oauthMutate`, session);
  }
  window.top.location.href = url;
}
