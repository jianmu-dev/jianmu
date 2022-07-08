import { ComponentPublicInstance, AppContext } from 'vue';
import { Router } from 'vue-router';
import { Store } from 'vuex';
import { IRootState } from '@/model';
import { HttpError, TimeoutError } from '@/utils/rest/error';
import { IErrorMessageVo } from '@/api/dto/common';
import dynamicRender from '@/utils/dynamic-render';
import { namespace as sessionNs } from '@/store/modules/session';
import LoginVerify from '@/views/login/dialog.vue';
import { checkLocation } from '@/utils/rest';

/**
 * 全局错误处理
 * @param error
 * @param instance
 * @param info
 * @param router
 * @param store
 */
export async function globalErrorHandler(
  error: Error, instance: ComponentPublicInstance | null, info: string | null,
  router: Router, store: Store<IRootState>) {
  const proxy = instance as any;

  if (error instanceof TimeoutError) {
    // 如果发送请求时的路由地址发生变化不做路由跳转
    if (!checkLocation(error.response)) {
      return;
    }
    await router.push({ name: 'network-error' });
    return;
  }

  if (error instanceof HttpError) {
    const { response } = error as HttpError;
    const { status, data } = response;

    switch (status) {
      case 400:
        proxy.$error((data as IErrorMessageVo).message);
        break;
      case 401: {
        // 清理token
        store.commit(`${sessionNs}/mutateDeletion`);
        // 动态渲染登录验证弹窗
        const appContext = instance?.$.appContext as AppContext;
        dynamicRender(LoginVerify, appContext);
        break;
      }
      case 403:
      case 404:
      case 500: {
        // 如果发送请求时的路由地址发生变化不做路由跳转
        if (!checkLocation(error.response)) {
          break;
        }
        await router.push({
          name: 'http-status-error',
          params: { value: status },
          query: { errMessage: error.response.data.message },
        });
        break;
      }
      // TODO 待扩展，处理其他错误码
    }
    return;
  }

  proxy.$error(`未知错误：${error.message}`);
  console.error(error);
  if (info) {
    console.error(info);
  }
}
