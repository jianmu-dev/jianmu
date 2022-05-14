import { IParamReference } from './data';

/**
 * 解析参数引用
 * @param raw 格式：${xxx.[inner.]xxx}
 */
export function parseParamReference(raw: string): IParamReference {
  const arr = raw.substring(2, raw.length - 1).split('.');
  return {
    ref: arr[arr.length - 1],
    nodeId: arr[0],
    inner: arr.length === 3,
    raw,
  };
}

/**
 * 提取参数引用
 * @param text
 */
export function extractParamReferences(text: string): IParamReference[] {
  // 格式：${xxx.[inner.]xxx}
  const matches = text.match(/\$\{[0-9a-zA-Z_]+.(inner.)?[0-9a-zA-Z_]+\}/g);
  if (!matches) {
    return [];
  }

  const set = new Set<string>();
  // 去重
  matches.forEach(match => set.add(match));

  return Array.from(set).map(raw => parseParamReference(raw));
}