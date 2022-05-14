// @ts-ignore
import listen from 'good-listener';
import { IParam, ISelectableParam } from './data';
import { ELEMENT_NODE_TYPE, NEW_LINE, RAW_ATTR_NAME, TEXT_NODE_TYPE } from './const';
import { calculateContentSize, extractReferences, fromArray, toContent } from './util';
import { ParamToolbar } from './param-toolbar';

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
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = value;
    // 因确定不了外部传进来的数据格式，强制转成纯文本
    this.refresh(tempDiv.innerText);

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
      if (el.tagName === 'INPUT' && el.parentNode && el.getAttribute(RAW_ATTR_NAME)) {
        this.toolbar.show(el as HTMLInputElement);
      }
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
    // disabled的input才兼容FF不可编辑input，否则（readonly），用左右键把光标定位到input中可敲键盘插入数据
    document.execCommand('insertHTML', false, this.getParamHtml(param));
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
    const children = plainText.trim().split(NEW_LINE).map((text => {
      const child = document.createElement('div');
      child.innerHTML = this.parseExp(text);

      return child;
    }));

    const tempDiv = document.createElement('div');
    tempDiv.append(...children);

    return tempDiv.innerHTML;
  }

  private getParamHtml(param: IParam): string {
    const { width, height } = calculateContentSize(this.editorEl.parentNode!, param);

    return `<input type="text" style="width: ${width}px; height: ${height}px;" disabled="disabled" value="${toContent(param)}" data-raw="${param.raw}"/>`;
  }

  private parseExp(plainText: string): string {
    const references = extractReferences(plainText);

    references.forEach(reference => {
      try {
        const param = this.toolbar.getParam(reference);
        plainText = plainText.replaceAll(reference.raw, this.getParamHtml(param));
      } catch (err) {
        console.warn(err.message);
      }
    });

    return plainText;
  }

  getPlainText(selectedEl: HTMLDivElement): string {
    let plainText = '';

    this.getLines(selectedEl).forEach(line => {
      Array.from(line.querySelectorAll('input')).forEach(input => {
        const textNode = document.createTextNode(input.getAttribute(RAW_ATTR_NAME)!);
        line.insertBefore(textNode, input);
        line.removeChild(input);
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