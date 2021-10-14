import rest, { IRequest } from '@/utils/rest';

export async function restProxy<T = any>(request: IRequest): Promise<T> {
  return await rest(request);
}