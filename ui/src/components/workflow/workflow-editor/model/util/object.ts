export function compare(origin: any, target: any): boolean {
  if (origin === null || target === null) {
    // origin或target为null
    return origin === target;
  }
  if (typeof target !== 'object') {
    // target不是对象/数组
    // 直接返回全等的比较结果
    return origin === target;
  }

  if (typeof origin !== 'object') {
    // origin不是对象/数组
    // 直接返回false
    return false;
  }
  for (const key of Object.keys(target)) {
    // 遍历target的所有自身属性的key
    if (!compare(origin[key], target[key])) {
      // 递归比较key对应的value，
      // value不等，则两对象不等，结束循环，退出函数，返回false
      return false;
    }
  }
  // 遍历结束，所有value都深度比较相等，则两对象相等
  return true;
}