import rest, { IRequest } from '@/utils/rest';
import { ConflictError } from '@/utils/rest/error';
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
    throw err;
  }
}
