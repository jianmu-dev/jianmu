// @ts-ignore
import listen from 'good-listener';

export interface IParam {
  ref: string;
  name: string;
  nodeId: string;
  nodeName: string;
}

export interface ISelectableParam {
  readonly value: string;
  readonly label: string;
  readonly children?: ISelectableParam[];
}

const TEXT_NODE_TYPE = 3;
const ELEMENT_NODE_TYPE = 1;
const NEW_LINE = '\n';
const RAW_ATTR_NAME = 'data-raw';

interface ISize {
  width: number;
  height: number;
}

function calcTextSize(container: Node, { nodeName, name }: IParam): ISize {
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

  container.appendChild(tempDiv);
  const width = tempSpan.offsetWidth;
  const height = tempSpan.offsetHeight;
  container.removeChild(tempDiv);

  return { width, height };
}

class ParamToolbar {
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

  getParam(nodeId: string, ref: string): IParam {
    const { label: nodeName, children } = this.selectableParams.find(({ value }) => value === nodeId)!;
    const { label: name } = children!.find(({ value }) => value === ref)!;

    return { ref, name, nodeId, nodeName };
  }

  getCurrentParam(): IParam | undefined {
    if (!this.paramRefEl) {
      return undefined;
    }

    // 格式：
    const data = this.paramRefEl.getAttribute(RAW_ATTR_NAME)!;
    const tempArr1 = this.paramRefEl.value.split('.');
    const tempArr2 = data.substring(2, data.length - 1).split('.');

    return {
      ref: tempArr2[1],
      name: tempArr1[1],
      nodeId: tempArr2[0],
      nodeName: tempArr1[0],
    };
  }

  updateParam(newVal: IParam): void {
    if (!this.paramRefEl) {
      return;
    }

    const { width, height } = calcTextSize(this.toolbarEl.parentNode!, newVal);
    this.paramRefEl.style.width = `${width}px`;
    this.paramRefEl.style.height = `${height}px`;

    this.paramRefEl.value = `${newVal.nodeName}.${newVal.name}`;
    this.paramRefEl.setAttribute(RAW_ATTR_NAME, `\${${newVal.nodeId}.${newVal.ref}}`);
  }
}

export class ExpressionEditor {
  readonly toolbar: ParamToolbar;
  private readonly editorEl: HTMLDivElement;
  private readonly observer: MutationObserver;
  private listener: any;

  constructor(paramToolbarEl: HTMLElement, editorEl: HTMLDivElement,
    value: string, selectableParams: ISelectableParam[]) {
    this.toolbar = new ParamToolbar(paramToolbarEl, selectableParams);
    this.editorEl = editorEl;
    this.editorEl.innerHTML = this.parse(value);

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
  }

  insertParam(nodeId: string, paramRef: string): void {
    this.validateSelectionInEditor();

    const param = this.toolbar.getParam(nodeId, paramRef);

    // disabled的input才兼容FF不可编辑input，否则（readonly），用左右键把光标定位到input中可敲键盘插入数据
    document.execCommand('insertHTML', false, this.getParamHtml(param));
  }

  private validateSelectionInEditor() {
    const selection = document.getSelection();
    if (!selection || !selection.anchorNode) {
      throw new Error('请确定要插入的位置');
    }

    let node = selection.anchorNode;
    // eslint-disable-next-line no-constant-condition
    while (true) {
      if (node === document.body) {
        throw new Error('请确定要插入的位置');
      }

      if (node === this.editorEl) {
        break;
      }

      const { parentNode } = node;
      if (!parentNode) {
        throw new Error('请确定要插入的位置');
      }

      node = parentNode;
    }
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

  private getParamHtml(param: IParam): string {
    const { ref, name, nodeId, nodeName } = param;
    const { width, height } = calcTextSize(this.editorEl.parentNode!, param);

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
      const param = this.toolbar.getParam(nodeId, paramRef);

      text = text.replaceAll(match, this.getParamHtml(param));
    });

    return text;
  }

  getRaw(selectedEl: HTMLDivElement): string {
    let raw = '';

    this.getLines(selectedEl).forEach(line => {
      Array.from(line.querySelectorAll('input')).forEach(input => {
        const textNode = document.createTextNode(input.getAttribute(RAW_ATTR_NAME)!);
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