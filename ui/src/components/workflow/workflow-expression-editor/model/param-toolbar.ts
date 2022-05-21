import { IParam, IParamReference, ISelectableParam } from './data';
import { RAW_ATTR_NAME } from './const';
import { calculateContentSize, fromRaw, getParam, toContent } from './util';

export class ParamToolbar {
  private readonly toolbarEl: HTMLElement;
  private readonly selectableParams: ISelectableParam[];
  private paramRefEl?: HTMLInputElement | HTMLTextAreaElement;
  private hideCallback?: () => void;

  constructor(toolbarEl: HTMLElement, selectableParams: ISelectableParam[]) {
    this.toolbarEl = toolbarEl;
    this.selectableParams = selectableParams;
  }

  refreshSelectableParams(selectableParams: ISelectableParam[]): void {
    if (this.selectableParams === selectableParams) {
      return;
    }
    this.selectableParams.length = 0;
    this.selectableParams.push(...selectableParams);
  }

  getParamRefEl(): HTMLInputElement | HTMLTextAreaElement | undefined {
    return this.paramRefEl;
  }

  show(paramRefEl: HTMLInputElement): void {
    this.paramRefEl = paramRefEl;
    const textEl = this.toolbarEl.firstElementChild!;
    textEl.className = `text${(paramRefEl.tagName === 'INPUT' ? ' single-line' : '')}`;
    textEl.innerHTML = paramRefEl.value;

    const { x, y, width, height } = this.paramRefEl.getBoundingClientRect();

    this.toolbarEl.style.left = `${x}px`;
    this.toolbarEl.style.top = `${y}px`;
    this.toolbarEl.style.width = `${width}px`;
    this.toolbarEl.style.height = `${height}px`;
  }

  hide(movingEl?: HTMLElement): void {
    if (movingEl) {
      if (movingEl === this.toolbarEl || movingEl.parentElement === this.toolbarEl) {
        // 鼠标在工具栏上移动时，保持显示
        return;
      }

      if (movingEl.className.includes('el-cascader') ||
        movingEl.className.includes('el-input') ||
        movingEl.className.includes('el-popper__arrow')
      ) {
        const cascaderRelavantEl = this.toolbarEl.querySelector('.el-cascader');
        const cascaderId = cascaderRelavantEl?.getAttribute('ariadescribedby');

        if (cascaderId) {
          // eslint-disable-next-line no-constant-condition
          while (true) {
            if (!movingEl) {
              break;
            }

            if (movingEl.id === cascaderId || movingEl === this.toolbarEl) {
              // 鼠标在工具栏上移动时，保持显示
              return;
            }

            movingEl = movingEl.parentElement || undefined;
          }
        }
      }
    }

    if (this.hideCallback) {
      this.hideCallback();
    }

    delete this.hideCallback;
    delete this.paramRefEl;

    this.toolbarEl.style.left = '';
    this.toolbarEl.style.top = '';
    this.toolbarEl.style.width = '';
    this.toolbarEl.style.height = '';
  }

  getParam(reference: IParamReference): IParam {
    return getParam(reference, this.selectableParams);
  }

  getCurrentParam(hideCallback: () => void): IParam | undefined {
    this.hideCallback = hideCallback;

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

    const parentNode = this.paramRefEl.parentNode!;
    const { width, height, multiline } = calculateContentSize(this.toolbarEl.parentNode!, param);
    if (multiline) {
      if (this.paramRefEl.tagName === 'TEXTAREA') {
        this.paramRefEl.style.width = `${width}px`;
        this.paramRefEl.style.height = `${height}px`;
        this.paramRefEl.innerText = toContent(param);
        this.paramRefEl.setAttribute(RAW_ATTR_NAME, param.raw);
      } else {
        // 替换
        const el = this.buildParamEl(param) as HTMLTextAreaElement;
        parentNode.insertBefore(el, this.paramRefEl);
        parentNode.removeChild(this.paramRefEl);
        this.paramRefEl = el;
      }
    } else {
      if (this.paramRefEl.tagName === 'INPUT') {
        this.paramRefEl.style.width = `${width}px`;
        this.paramRefEl.style.height = `${height}px`;
        this.paramRefEl.value = toContent(param);
        this.paramRefEl.setAttribute(RAW_ATTR_NAME, param.raw);
      } else {
        // 替换
        const el = this.buildParamEl(param) as HTMLTextAreaElement;
        parentNode.insertBefore(el, this.paramRefEl);
        parentNode.removeChild(this.paramRefEl);
        this.paramRefEl = el;
      }
    }
  }

  buildParamEl(param: IParam): HTMLInputElement | HTMLTextAreaElement {
    const { width, height, multiline } = calculateContentSize(this.toolbarEl.parentNode!, param);

    let el: HTMLInputElement | HTMLTextAreaElement;
    if (multiline) {
      const textareaEl = document.createElement('textarea');
      textareaEl.innerText = toContent(param);

      el = textareaEl;
    } else {
      const inputEl = document.createElement('input');
      inputEl.type = 'text';
      inputEl.setAttribute('value', toContent(param));

      el = inputEl;
    }

    el.className = 'param-ref';
    el.style.width = `${width}px`;
    el.style.height = `${height}px`;
    el.setAttribute('disabled', 'disabled');
    el.setAttribute('data-raw', param.raw);

    return el;
  }
}