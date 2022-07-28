import rest, { IRequest } from '@/utils/rest';
import { ConflictError } from '@/utils/rest/error';
import { namespace } from '@/store/modules/session';
import _store from '@/store';
import { ISessionVo } from '@/api/dto/session';

export async function restProxy<T = any>(request: IRequest): Promise<T> {
  try {
    return await rest(request);
  } catch (err) {
    if (err instanceof ConflictError) {
      const session = err.response.data as ISessionVo;
      _store.commit(`${namespace}/oauthMutate`, session);
      return await rest(request);
    }
    throw err;
  }
}
