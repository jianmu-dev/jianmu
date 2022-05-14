import { IContentSize, IParam, IParamReference } from './data';

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

/**
 * 转成显示内容
 * @param param
 */
export function toContent(param: IParam): string {
  const { name, nodeName, inner } = param;
  return `${nodeName}.${inner ? '内置输出参数.' : ''}${name}`;
}

/**
 * 转成原始数据
 * @param val
 */
export function toRaw(val: IParamReference | string[]): string {
  let ref, nodeId, inner;
  if (val instanceof Array) {
    nodeId = val[0];
    ref = val[val.length - 1];
    inner = val.length === 3;
  } else {
    nodeId = val.nodeId;
    ref = val.ref;
    inner = val.inner;
  }
  return `${nodeId}.${inner ? 'inner.' : ''}${ref}`;
}

/**
 * 计算显示内容大小
 * @param container
 * @param param
 */
export function calculateContentSize(container: Node, param: IParam): IContentSize {
  const tempDiv = document.createElement('div');
  tempDiv.className = 'jm-workflow-expression-editor';
  tempDiv.style.position = 'fixed';
  tempDiv.style.left = '-1000px';
  tempDiv.style.top = '-1000px';
  tempDiv.style.whiteSpace = 'nowrap';
  tempDiv.style.visibility = 'hidden';

  const tempSpan = document.createElement('span');
  tempSpan.style.padding = '0';
  tempSpan.style.borderWidth = '0';
  tempSpan.innerText = toContent(param);
  tempDiv.appendChild(tempSpan);

  container.appendChild(tempDiv);
  const width = tempSpan.offsetWidth;
  const height = tempSpan.offsetHeight;
  container.removeChild(tempDiv);

  return { width, height };
}