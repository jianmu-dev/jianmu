import { IParam, IParamReference, ISelectableParam } from './data';
import { RAW_ATTR_NAME } from './const';
import { calculateContentSize, fromRaw, getParam, toContent } from './util';

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
    return getParam(reference, this.selectableParams);
  }

  getCurrentParam(): IParam | undefined {
    if (!this.paramRefEl) {
      return undefined;
    }

    const raw = this.paramRefEl.getAttribute(RAW_ATTR_NAME)!;
    return this.getParam(fromRaw(raw));
  }

  updateParam(param: IParam): void {
    if (!this.paramRefEl) {
      return;
    }

    const { width, height } = calculateContentSize(this.toolbarEl.parentNode!, param);
    this.paramRefEl.style.width = `${width}px`;
    this.paramRefEl.style.height = `${height}px`;

    this.paramRefEl.value = toContent(param);
    this.paramRefEl.setAttribute(RAW_ATTR_NAME, param.raw);
  }
}