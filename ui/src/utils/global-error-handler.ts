import { ComponentPublicInstance } from 'vue';
import { Router } from 'vue-router';
import { Store } from 'vuex';
import { IRootState } from '@/model';
import { HttpError, TimeoutError } from '@/utils/rest/error';
import { IErrorMessageVo } from '@/api/dto/common';
import { LOGIN_INDEX } from '@/router/path-def';
import JmMessageBox from '@/components/notice/message-box';
import { namespace as sessionNs } from '@/store/modules/session';

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
      case 401:
        // 清理token
        store.commit(`${sessionNs}/mutateDeletion`);

        JmMessageBox.confirm('此操作需要登录, 是否继续？', '尚未登录', {
          confirmButtonText: '登录',
          cancelButtonText: '取消',
          type: 'info',
        }).then(async () => {
          await router.push({
            name: 'login',
            query: {
              redirectUrl:
                router.currentRoute.value.path === LOGIN_INDEX ? undefined :
                  router.currentRoute.value.fullPath,
            },
          });
        }).catch(() => {
        });
        break;
      case 404:
      case 500:
        await router.push({ name: 'http-status-error', params: { value: status } });
        break;
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