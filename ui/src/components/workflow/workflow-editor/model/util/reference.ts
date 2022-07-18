import { RefTypeEnum } from '../data/enumeration';

/**
 * 检查唯一标识是否重复
 * @param refs
 * @param type
 */
export function checkDuplicate(refs: string[], type: RefTypeEnum): void {
  const countObj = refs.reduce<Record<string, number>>((obj, ref) => {
    if (ref in obj) {
      obj[ref]++;
    } else {
      obj[ref] = 1;
    }
    return obj;
  }, {});
  for (const ref in countObj) {
    if (countObj[ref] > 1 && ref !== '') {
      throw new Error(`${type}唯一标识 "${ref}" 重复`);
    }
  }
}