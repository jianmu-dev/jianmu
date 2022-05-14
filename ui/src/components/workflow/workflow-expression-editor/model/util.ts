import { IContentSize, IParam, IParamReference, ISelectableParam } from './data';
import { INNER_PARAM_TAG } from './const';
import { NodeError, ParamError } from './error';

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
 * @param plainText
 */
export function extractReferences(plainText: string): IParamReference[] {
  // 格式：${xxx.[inner.]xxx}
  const matches = plainText.match(/\$\{[0-9a-zA-Z_]+\.(inner\.)?[0-9a-zA-Z_]+\}/g);
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

/**
 * 获取参数
 * @param reference
 * @param selectableParams
 * @throws 找不到节点或参数时，抛异常
 */
export function getParam(reference: IParamReference, selectableParams: ISelectableParam[]): IParam {
  const { ref, nodeId, inner, raw } = reference;

  const node = selectableParams.find(({ value }) => value === nodeId);
  if (!node) {
    // 节点不存在
    throw new NodeError(reference);
  }
  const { label: nodeName, children } = node;
  const param = (inner ? children!.find(({ value }) => value === INNER_PARAM_TAG)!.children : children)!
    .find(({ value }) => value === ref);
  if (!param) {
    throw new ParamError(reference);
  }
  const { label: name } = param;

  return { ref, name, nodeId, nodeName, inner, raw };
}