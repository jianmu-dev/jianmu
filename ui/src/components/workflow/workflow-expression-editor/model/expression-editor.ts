// @ts-ignore
import listen from 'good-listener';
import { v4 as uuidv4 } from 'uuid';
import { ISelectableParam } from './data';
import { ELEMENT_NODE_TYPE, NEW_LINE, RAW_ATTR_NAME, TEXT_NODE_TYPE } from './const';
import { extractReferences, fromArray } from './util';
import { ParamToolbar } from './param-toolbar';
import { NodeError, ParamError } from './error';

export class ExpressionEditor {
  readonly toolbar: ParamToolbar;
  private readonly editorEl: HTMLDivElement;
  private readonly observer: MutationObserver;
  private listener: any;
  private lastRange?: Range;

  constructor(paramToolbarEl: HTMLElement, editorEl: HTMLDivElement,
    value: string, selectableParams: ISelectableParam[]) {
    this.toolbar = new ParamToolbar(paramToolbarEl, selectableParams);
    this.editorEl = editorEl;
    this.refresh(value);

    this.observer = new MutationObserver(() => {
      const paramRefEl = this.toolbar.getParamRefEl();
      if (!paramRefEl || paramRefEl.parentNode) {
        return;
      }

      // 当工具栏显示在某个参数引用上时，删除参数引用要隐藏工具栏
      this.toolbar.hide();
    });
    this.observer.observe(this.editorEl, { childList: true, subtree: true });

    // 必须是mousemove，才能识别disabled的input
    this.listener = listen(document.body, 'mousemove', (e: MouseEvent) => {
      if (!e.target) {
        return;
      }

      const el = e.target as HTMLElement;
      if (['TEXTAREA', 'INPUT'].includes(el.tagName) && el.parentNode && el.getAttribute(RAW_ATTR_NAME)) {
        this.toolbar.show(el as HTMLInputElement);
        return;
      }

      this.toolbar.hide(el);
    });
  }

  destroy(): void {
    this.observer.disconnect();
    this.listener.destroy();
    delete this.lastRange;
  }

  refreshLastRange(): void {
    const selection = document.getSelection();
    if (!selection || selection.rangeCount === 0) {
      delete this.lastRange;
      return;
    }

    this.lastRange = selection.getRangeAt(0);
  }

  insertParam(arr: string[]): void {
    this.editorEl.focus();

    const selection = document.getSelection();
    if (selection && this.lastRange) {
      selection.removeAllRanges();
      selection.addRange(this.lastRange);
    }

    const param = this.toolbar.getParam(fromArray(arr));
    const paramRefEl = this.toolbar.buildParamEl(param);
    const isTextarea = paramRefEl.tagName === 'TEXTAREA';
    if (isTextarea) {
      // insertHTML无法正常插入文本域的内容，通过临时设置id，后续指定
      paramRefEl.id = uuidv4();
    }
    // disabled的input才兼容FF不可编辑input，否则（readonly），用左右键把光标定位到input中可敲键盘插入数据
    document.execCommand('insertHTML', false, paramRefEl.outerHTML);

    if (isTextarea) {
      // insertHTML无法正常插入文本域的内容，通过临时设置id，后续指定
      const tempEl = document.getElementById(paramRefEl.id)!;
      tempEl.innerText = paramRefEl.innerText;
      tempEl.removeAttribute('id');
    }
  }

  checkManualInput(plainText: string): boolean {
    const references = extractReferences(plainText);
    for (const reference of references) {
      try {
        this.toolbar.getParam(reference);
        // 可获取参数表示手动输入了参数引用
        return true;
      } catch (err) {
        if (err instanceof NodeError || err instanceof ParamError) {
          // 忽略日志，继续
          continue;
        }
        throw err;
      }
    }
    return false;
  }

  cut(e: ClipboardEvent): void {
    this.copy(e);

    // 删除选中，达到剪切效果
    document.execCommand('delete', false);
  }

  copy(e: ClipboardEvent): void {
    // 阻止默认复制
    e.preventDefault();
    if (!e.clipboardData) {
      return;
    }

    const selection = document.getSelection();
    if (!selection) {
      return;
    }

    const fragment = selection.getRangeAt(0).cloneContents();
    if (fragment.childNodes.length === 0) {
      // 没有选中任何内容，忽略
      return;
    }

    const tempDiv = document.createElement('div');
    tempDiv.append(fragment);
    e.clipboardData.setData('text/plain', this.getPlainText(tempDiv));
  }

  paste(e: ClipboardEvent): void {
    // 阻止默认粘贴
    e.preventDefault();
    if (!e.clipboardData) {
      return;
    }

    // 插入html
    document.execCommand('insertHTML', false,
      this.buildHtml(e.clipboardData.getData('text/plain')));
  }

  refresh(plainText: string): void {
    this.editorEl.innerHTML = this.buildHtml(plainText);
  }

  private buildHtml(plainText: string): string {
    plainText = plainText.trim();
    if (!plainText) {
      return '';
    }

    let tempDiv = document.createElement('div');
    const children = plainText.split(NEW_LINE).map((pText => {
      tempDiv.innerText = pText;
      // escape html
      pText = tempDiv.innerHTML;

      const child = document.createElement('div');
      child.innerHTML = this.parse(pText);

      return child;
    }));

    tempDiv = document.createElement('div');
    tempDiv.append(...children);

    return tempDiv.innerHTML;
  }

  private parse(plainText: string): string {
    const references = extractReferences(plainText);

    references.forEach(reference => {
      try {
        const param = this.toolbar.getParam(reference);
        plainText = plainText.replaceAll(reference.raw, this.toolbar.buildParamEl(param).outerHTML);
      } catch (err) {
        console.warn(err.message);
      }
    });

    return plainText || '<br/>';
  }

  getPlainText(selectedEl: HTMLDivElement): string {
    let plainText = '';

    this.getLines(selectedEl).forEach(line => {
      Array.from(line.querySelectorAll('.param-ref')).forEach(paramRefEl => {
        const textNode = document.createTextNode(paramRefEl.getAttribute(RAW_ATTR_NAME)!);
        line.insertBefore(textNode, paramRefEl);
        line.removeChild(paramRefEl);
      });

      plainText += line.innerText + NEW_LINE;
    });

    return plainText.trim();
  }

  private getLines(selectedEl: HTMLDivElement, lines: HTMLDivElement[] = []): HTMLDivElement[] {
    const { childNodes } = selectedEl;

    if (childNodes.length === 1 && childNodes.item(0).nodeType === TEXT_NODE_TYPE) {
      // 表示只有一个text子节点
      lines.push(selectedEl);

      return lines;
    }

    let previousDivIndex = -1;
    const children = Array.from(childNodes);
    children.forEach((child, index) => {
      if (child.nodeType !== ELEMENT_NODE_TYPE || (child as Element).tagName !== 'DIV') {
        return;
      }
      // 表示当前为div

      if (index > previousDivIndex + 1) {
        const nodes = children.slice(previousDivIndex + 1, index);
        const tempDiv = document.createElement('div');
        tempDiv.append(...nodes);

        lines.push(tempDiv);
      }

      this.getLines(child as HTMLDivElement, lines);

      previousDivIndex = index;
    });

    if (previousDivIndex === -1) {
      // 表示只有text和input子节点
      lines.push(selectedEl);
    }

    return lines;
  }
}