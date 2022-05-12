// @ts-ignore
import listen from 'good-listener';

export interface IParam {
  ref: string;
  name: string;
  nodeId: string;
  nodeName: string;
}

export type GetParamFn = (nodeId: string, paramRef: string) => IParam;

const TEXT_NODE_TYPE = 3;
const ELEMENT_NODE_TYPE = 1;
const NEW_LINE = '\n';

export class ExpressionEditor {
  private readonly editorEl: HTMLDivElement;
  private readonly getParam: GetParamFn;
  private listener: any;

  constructor(editorEl: HTMLDivElement, value: string, getParam: GetParamFn) {
    this.editorEl = editorEl;
    this.getParam = getParam;
    this.editorEl.innerHTML = this.parse(value);

    // 必须是mousemove，才能识别disabled的input
    this.listener = listen(document.body, 'mousemove', (e: MouseEvent) => {
      if (!e.target) {
        return;
      }

      const el = e.target as HTMLElement;
      if (el.tagName === 'INPUT' && el.parentNode) {
        el.className = 'input-hover';
      }

      Array.from(this.editorEl.querySelectorAll('.input-hover')).forEach(input => {
        if (input === el) {
          return;
        }

        input.className = '';
      });
    });
  }

  destroy(): void {
    this.listener.destroy();
  }

  insertParam(nodeId: string, paramRef: string): void {
    const selection = document.getSelection();
    if (!selection || selection.anchorNode !== this.editorEl) {
      throw new Error('请确定要插入的位置');
    }

    const param = this.getParam(nodeId, paramRef);

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
    e.clipboardData.setData('text/plain', this.getRaw(tempDiv));
  }

  paste(e: ClipboardEvent): void {
    // 阻止默认粘贴
    e.preventDefault();
    if (!e.clipboardData) {
      return;
    }

    const children = e.clipboardData.getData('text/plain')
      .split(NEW_LINE)
      .map((text => {
        const child = document.createElement('div');
        child.innerHTML = this.parse(text);

        return child;
      }));

    const tempDiv = document.createElement('div');
    tempDiv.append(...children);

    // 插入html
    document.execCommand('insertHTML', false, tempDiv.innerHTML);
  }

  private getParamHtml({ ref, name, nodeId, nodeName }: IParam): string {
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
    tempSpan.innerText = `${nodeName}.${name}`;
    tempDiv.appendChild(tempSpan);

    this.editorEl.parentNode!.appendChild(tempDiv);
    const width = tempSpan.offsetWidth;
    const height = tempSpan.offsetHeight;
    this.editorEl.parentNode!.removeChild(tempDiv);

    return `<input type="text" style="width: ${width}px; height: ${height}px;" disabled="disabled" value="${nodeName}.${name}" data-raw="\${${nodeId}.${ref}}"/>`;
  }

  private parse(text: string): string {
    // 提取所有${xxx.xxx}
    const matches = text.match(/\$\{[0-9a-zA-Z_]+\.[0-9a-zA-Z_]+\}/g);
    if (!matches) {
      return text;
    }

    const set = new Set<string>();
    // 去重
    matches.forEach(match => set.add(match));

    set.forEach(match => {
      const arr = match.substring(2, match.length - 1).split('.');
      const nodeId = arr[0];
      const paramRef = arr[1];
      const param = this.getParam(nodeId, paramRef);

      text = text.replaceAll(match, this.getParamHtml(param));
    });

    return text;
  }

  getRaw(selectedEl: HTMLDivElement): string {
    let raw = '';

    this.getLines(selectedEl).forEach(line => {
      Array.from(line.querySelectorAll('input')).forEach(input => {
        const textNode = document.createTextNode(input.getAttribute('data-raw')!);
        line.insertBefore(textNode, input);
        line.removeChild(input);
      });

      raw += line.innerText + NEW_LINE;
    });

    return raw;
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