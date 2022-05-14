import { IContentSize, IParam, IParamReference } from './data';
import { INNER_PARAM_TAG } from './const';

/**
 * 解析参数引用
 * @param arr
 */
export function fromArray(arr: string[]): IParamReference {
  const ref = arr[arr.length - 1];
  const nodeId = arr[0];
  const inner = arr.length === 3;
  const raw = `\${${nodeId}.${inner ? `${INNER_PARAM_TAG}.` : ''}${ref}}`;

  return { ref, nodeId, inner, raw };
}

/**
 * 解析参数引用
 * @param raw 格式：${xxx.[inner.]xxx}
 */
export function fromRaw(raw: string): IParamReference {
  const arr = raw.substring(2, raw.length - 1).split('.');
  return fromArray(arr);
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
 * 提取参数引用
 * @param text
 */
export function extractReferences(text: string): IParamReference[] {
  // 格式：${xxx.[inner.]xxx}
  const matches = text.match(/\$\{[0-9a-zA-Z_]+.(inner.)?[0-9a-zA-Z_]+\}/g);
  if (!matches) {
    return [];
  }

  const set = new Set<string>();
  // 去重
  matches.forEach(match => set.add(match));

  return Array.from(set).map(raw => fromRaw(raw));
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