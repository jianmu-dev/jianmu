import { IParam, IParamReference, ISelectableParam } from './data';
import { INNER_PARAM_TAG, RAW_ATTR_NAME } from './const';
import { parseParamReference } from './util';

export interface ISize {
  width: number;
  height: number;
}

export class ParamToolbar {
  private readonly toolbarEl: HTMLElement;
  private readonly selectableParams: ISelectableParam[];
  private paramRefEl?: HTMLInputElement;

  constructor(toolbarEl: HTMLElement, selectableParams: ISelectableParam[]) {
    this.toolbarEl = toolbarEl;
    this.selectableParams = selectableParams;
  }

  getParamRefEl(): HTMLInputElement | undefined {
    return this.paramRefEl;
  }

  show(paramRefEl: HTMLInputElement): void {
    this.paramRefEl = paramRefEl;
    this.toolbarEl.firstElementChild!.innerHTML = paramRefEl.value;

    const { x, y, width, height } = this.paramRefEl.getBoundingClientRect();

    this.toolbarEl.style.left = `${x}px`;
    this.toolbarEl.style.top = `${y}px`;
    this.toolbarEl.style.width = `${width}px`;
    this.toolbarEl.style.height = `${height}px`;
  }

  hide(): void {
    delete this.paramRefEl;

    this.toolbarEl.style.left = '';
    this.toolbarEl.style.top = '';
    this.toolbarEl.style.width = '';
    this.toolbarEl.style.height = '';
  }

  getParam(reference: IParamReference): IParam {
    const { ref, nodeId, inner, raw } = reference;

    const node = this.selectableParams.find(({ value }) => value === nodeId);
    if (!node) {
      // 节点不存在
      throw new Error(`参数引用\${${this.toRaw(reference)}}对应的节点不存在`);
    }
    const { label: nodeName, children } = node;
    const param = (inner ? children!.find(({ value }) => value === INNER_PARAM_TAG)!.children : children)!
      .find(({ value }) => value === ref);
    if (!param) {
      throw new Error(`参数引用\${${this.toRaw(reference)}}对应的参数不存在`);
    }
    const { label: name } = param;

    return { ref, name, nodeId, nodeName, inner, raw };
  }

  getCurrentParam(): IParam | undefined {
    if (!this.paramRefEl) {
      return undefined;
    }

    const raw = this.paramRefEl.getAttribute(RAW_ATTR_NAME)!;
    return this.getParam(parseParamReference(raw));
  }

  updateParam(newVal: IParam): void {
    if (!this.paramRefEl) {
      return;
    }

    const { width, height } = this.calcTextSize(this.toolbarEl.parentNode!, newVal);
    this.paramRefEl.style.width = `${width}px`;
    this.paramRefEl.style.height = `${height}px`;

    this.paramRefEl.value = this.toValue(newVal);
    this.paramRefEl.setAttribute(RAW_ATTR_NAME, `\${${this.toRaw(newVal)}}`);
  }

  toValue(param: IParam): string {
    const { name, nodeName, inner } = param;
    return `${nodeName}.${inner ? '内置输出参数.' : ''}${name}`;
  }

  toRaw(val: IParamReference | string[]): string {
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

  calcTextSize(container: Node, param: IParam): ISize {
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
    tempSpan.innerText = this.toValue(param);
    tempDiv.appendChild(tempSpan);

    container.appendChild(tempDiv);
    const width = tempSpan.offsetWidth;
    const height = tempSpan.offsetHeight;
    container.removeChild(tempDiv);

    return { width, height };
  }
}