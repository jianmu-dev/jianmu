import rest, { IRequest } from '@/utils/rest';
import { ConflictError, HttpError } from '@/utils/rest/error';
import { ISession } from '@/model/modules/session';
import { namespace } from '@/store/modules/session';
import _store from '@/store';
import { setAuthCookie } from '@/utils/cookie';

export async function restProxy<T = any>(request: IRequest): Promise<T> {
  try {
    return await rest(request);
  } catch (err) {
    if (err instanceof ConflictError) {
      const session = err.response.data as ISession;
      setAuthCookie(session);
      _store.commit(`${namespace}/mutateSession`);
      return await rest(request);
    }

    if (err instanceof HttpError && err.response.status === 401) {
      console.log('尝试同步一次cookie');
      // 可能因store中的session过期导致401
      // 尝试同步一次cookie
      _store.commit(`${namespace}/mutateSession`);
      return await rest(request);
    }
    throw err;
  }
}
