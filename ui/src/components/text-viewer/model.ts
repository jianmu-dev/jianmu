import { createVNode, nextTick, render, Ref, AppContext, VNode, ref } from 'vue';
import TransitCalculator from './transit-calculator.vue';
import { v4 as uuidv4 } from 'uuid';
import Tip from './tip.vue';
import TextLine from './text-line.vue';
import _throttle from 'lodash/throttle';

export interface ICallbackEvent {
  contentMaxWidth: number;
}

interface ISize {
  height: number;
  width: number;
}

/**
 * 将带有html标签的字符串转换成纯文本
 * @param html
 */
function getPlainText(html: string): string {
  const div = document.createElement('div');
  div.innerHTML = html;
  return div.innerText;
}

type Callback = (event: ICallbackEvent) => void;

export class TextViewer {
  // 截流的阈值
  private readonly threshold: number;
  // 传入value的原始值，可能包含html元素标签
  private readonly value: string;
  // 去除html标签后的字符串
  private readonly plainText: string;
  // tooltip组件显示的位置
  private readonly tipPlacement: string;
  // 是否将tooltip放入body
  private readonly tipAppendToBody: boolean;
  // 控制tooltip的显示与隐藏
  private readonly tipVisible: Ref<boolean> = ref<boolean>(false);
  // 解决element组件未被注册的警告提示
  private readonly appContext: AppContext;
  // 用于计算宽度的临时文本内容
  private readonly temporaryContent: Ref<string> = ref<string>('');
  // 中转计算元素
  private readonly transitCalculator: HTMLElement;
  // transitCalculator元素id
  private readonly id: string;
  // 组件元素
  private readonly wrapperElement: HTMLElement;
  // 事件监听器
  private readonly resizeObserver: ResizeObserver;
  // 组件外层容器宽度变化触发的回调函数
  private readonly callback: Callback;
  // 身略号占据的宽度
  private ellipsisWidth: number = 0;
  // 文本占据的行高
  private lineHeight: number = 0;
  // 应用组件的外层元素的宽高
  private wrapperSize: ISize = { height: 0, width: 0 };

  constructor(
    value: string,
    tipPlacement: string,
    tipAppendToBody: boolean,
    wrapperElement: HTMLElement,
    appContext: AppContext,
    callback: Callback,
    threshold: number,
  ) {
    this.value = value;
    this.plainText = getPlainText(this.value);
    this.appContext = appContext;
    this.tipPlacement = tipPlacement;
    this.tipAppendToBody = tipAppendToBody;
    this.wrapperElement = wrapperElement;
    this.threshold = threshold;
    this.id = uuidv4();
    // 创建用于临时中转计算的元素
    const TransitWrapper = document.createElement('div');
    TransitWrapper.setAttribute('class', 'transit-wrapper');
    const transitCalculatorNode = createVNode(TransitCalculator, {
      'temporary-content': this.temporaryContent,
      id: this.id,
    });
    // 页面渲染临时中转计算的元素
    render(transitCalculatorNode, TransitWrapper);
    this.wrapperElement.appendChild(TransitWrapper);
    this.transitCalculator = document.getElementById(`${this.id}`)!;
    this.callback = callback;
    this.resizeObserver = new ResizeObserver(
      _throttle(async (entries: ResizeObserverEntry[]) => {
        const { width, height } = entries[0].contentRect;
        // 元素被隐藏宽高会变成0，如果宽高都为0本身也没必要进行监听
        if (!width && !height) {
          return;
        }
        // 如果宽度没发生变化，不需要重新计算
        if (width === this.wrapperSize.width) {
          return;
        }
        await this.reload();
      }, this.threshold),
    );
    this.resizeObserver.observe(this.wrapperElement!.parentElement as Element);
  }

  destroy(): void {
    this.resizeObserver.disconnect();
  }

  /**
   * 计算单个文字最大宽度
   * @param value
   * @param i
   * @param w
   * @return 最大宽度
   */
  private async calculateMaxCharWidth(value: string, i = 0, w = 0): Promise<number> {
    this.temporaryContent.value = value[i];
    await nextTick();
    const maxWidth = Math.max(w, this.transitCalculator.getBoundingClientRect().width);
    // 比较到字符内容的最后一位的字符得到最大的宽度值，结束递归。
    if (i === value.length - 1) {
      this.temporaryContent.value = '';
      return maxWidth;
    }
    return await this.calculateMaxCharWidth(value, i + 1, maxWidth);
  }

  /**
   * 计算传入内容会占据的最大宽度
   * @param value
   * @return 传入文本在页面会占据的最大宽度
   */
  private async calculateContentMaxWidth(value?: string): Promise<number> {
    this.temporaryContent.value = value || this.plainText;
    await nextTick();
    // getBoundingClientRect获取元素的宽度能精确到小数点后三位
    const maxWidth = this.transitCalculator.getBoundingClientRect().width;
    this.temporaryContent.value = '';
    return Math.ceil(maxWidth);
  }

  /**
   * 计算...的宽度
   */
  private async calculateEllipsisWidth(): Promise<number> {
    this.temporaryContent.value = '...';
    await nextTick();
    const ellipsisWidth = this.transitCalculator.getBoundingClientRect().width;
    this.temporaryContent.value = '';
    return ellipsisWidth;
  }

  /**
   * 计算行高
   * @return 行高
   */
  private async calculateLineHeight(): Promise<number> {
    this.temporaryContent.value = '.';
    await nextTick();
    this.temporaryContent.value = '';
    // 火狐浏览器计算的offsetHeight精确到小数两位，会大1px
    return this.transitCalculator.getBoundingClientRect().height;
  }

  /**
   * 计算容器大小
   * @param lineHeight 行高
   * @return 容器大小
   */
  private calculateContainerSize(lineHeight: number): Readonly<ISize> {
    const { width, height } = this.wrapperElement!.getBoundingClientRect();
    return {
      width: width,
      height: height || lineHeight,
    };
  }

  /**
   * 生成vNode中的文本
   * @param wrapperSize 容器大小
   * @param value 文本内容
   * @param rows 行数
   * @param i
   * @param contentArr
   * @return 保存的所有vNode文本数组
   */
  private async generateVNodesInnerText(
    wrapperSize: Readonly<ISize>,
    value: string,
    rows: number,
    i = 0,
    contentArr: string[] = [],
  ): Promise<string[]> {
    this.temporaryContent.value += value[i];
    await nextTick();
    // 获取到此时用于计算的元素中的文字在页面中占据的实际宽度
    let contentWidth = this.transitCalculator.getBoundingClientRect().width;
    // 提前先判断最后剩余的文本内容占据的宽度是否会大于外层容器的宽度，若大于外层容器的宽度，控制只让最后一行文本是带着...去计算生成截取的文本
    if (rows === contentArr.length + 1 && (await this.calculateLastLineMaxWidth(contentArr)) > wrapperSize.width) {
      contentWidth += this.ellipsisWidth;
    }
    // 截取到字符串的offsetWidth大于外层元素设置的宽度
    if (contentWidth > wrapperSize.width) {
      // 用于计算的元素中的文本的内容的倒数第二位的文本内容加入数组中，因为此时文本内容已经超过外层盒子的宽度，所以应当减一位。
      contentArr.push(this.temporaryContent.value.substring(0, this.temporaryContent.value.length - 1));
      // 文本内容截取后将响应式占位宽度的span内容清空
      this.temporaryContent.value = '';
      // 如果生成的文本内容数组长度和指定的行数相等递归结束。
      if (rows === contentArr.length) {
        return contentArr;
      }
      // 当生成的文本内容数组小于指定的行数时，继续重复步骤生成文本字段。
      return await this.generateVNodesInnerText(wrapperSize, value, rows, i, contentArr);
    }
    // 截取到字符串最后一位递归结束（当rows计算出得特别大也会在追加到最后一个字符后终止递归）
    if (i === value.length - 1) {
      contentArr.push(this.temporaryContent.value);
      // 文本内容截取后将响应式占位宽度的span内容清空
      this.temporaryContent.value = '';
      return contentArr;
    }
    // 当行追加内容
    return await this.generateVNodesInnerText(wrapperSize, value, rows, i + 1, contentArr);
  }

  /**
   * 计算最后一行文本内容占据的最大宽度
   * @param contentArr
   * @return 最后一行文本内容占据的最大宽度
   */
  private async calculateLastLineMaxWidth(contentArr: string[]): Promise<number> {
    // 将此时用于占据计算元素中的值当成临时变量保存。
    const temp = this.temporaryContent.value;
    // 获取此时保存在文本数组中的所有文本长度
    const contentLength = contentArr.reduce((pre, current) => {
      return pre + current.length;
    }, 0);
    // 得到最后一行中的文本内容
    const lastLineValue = this.plainText.substring(contentLength);
    // 获取最后一行文本所占据的宽度值
    const lastLineMaxWidth = await this.calculateContentMaxWidth(lastLineValue);
    this.temporaryContent.value = temp;
    return lastLineMaxWidth;
  }

  /**
   * 生成最终需要渲染到页面的VNode
   * @param contentArr vNode中的文本内容数组
   * @param rows 行数
   * @param lineHeight 行高
   * @return vNode
   */
  private async generateVNode(contentArr: string[], rows: number, lineHeight: number): Promise<VNode> {
    // 如果数组中没有内容证明没有达到指定宽度，不做操作
    if (contentArr.length === 0) {
      return createVNode('div', { class: 'content' });
    }
    const vNodes: VNode[] = [];
    for (let i = 0; i < rows; i++) {
      // 如果没有字符内容，进行下次
      if (!contentArr[i]) {
        break;
      }
      // 创建tooltip的VNode
      const toolTipVNode = createVNode(
        Tip,
        {
          effect: 'dark',
          contentText: this.value,
          placement: this.tipPlacement,
          style: { cursor: 'default' },
          appendToBody: this.tipAppendToBody,
          visible: this.tipVisible,
        },
        null,
      );
      toolTipVNode.appContext = this.appContext;
      // 创建每行文本的vNode
      const textLineVNode = createVNode(TextLine, {
        value: contentArr[i],
        style: { height: `${lineHeight}px` },
      });
      // 对非最后一行生成其VNode
      if (i < Math.min(contentArr.length, rows) - 1) {
        // 循环向vNode中追加文本，只有vNode中存在内容时才添加，减少没有意义的vNode
        vNodes.push(textLineVNode);
        continue;
      }
      // 对contentArr的最后一项的进行单独的判断，如果数组中的最后一位长度刚好是传入进来的值证明最后一行文本没有溢出，不需要生成toolTipVNode
      const contentLength = contentArr.reduce((pre, current) => {
        return pre + current.length;
      }, 0);
      if (contentLength === this.plainText.length) {
        vNodes.push(textLineVNode);
        continue;
      }
      // 创建出...的vNode
      vNodes.push(
        createVNode(
          'span',
          {
            class: 'text-line-last',
            style: { height: `${lineHeight}px` },
            // 挂载vNode后监听鼠标的移动情况，控制tooltip的显示与隐藏
            onVnodeMounted: (v: VNode) => {
              // 去除因为触控板快捷切换页面，tooltip不消失问题
              window.addEventListener('popstate', () => {
                this.tipVisible.value = false;
              });
              v.el?.parentElement.addEventListener('mouseenter', () => {
                this.tipVisible.value = true;
              });
              // 单独处理点击事件，因为文本组件应用在链接的地方，会导致tooltip不会消失的问题
              v.el?.parentElement.addEventListener('click', () => {
                this.tipVisible.value = false;
              });
              v.el?.parentElement.addEventListener('mouseleave', () => {
                this.tipVisible.value = false;
              });
            },
          },
          [textLineVNode, toolTipVNode],
        ),
      );
    }
    return createVNode('div', { class: 'content' }, vNodes);
  }

  private async init(value = ''): Promise<void> {
    if (value.length === 0) {
      // 如果传入的内容为空字符串，生成一个类名为content的空div 作为VNode
      const wrapperNode = await this.generateVNode([], 0, this.lineHeight);
      render(wrapperNode, this.wrapperElement);
      return;
    }
    // 防止在组件外部使用flex布局，造成宽度为0
    if (!this.wrapperSize.width) {
      return;
    }
    let isOverFlow = false;
    this.temporaryContent.value = this.plainText;
    await nextTick();
    isOverFlow = this.transitCalculator.getBoundingClientRect().width > this.wrapperSize.width;
    this.temporaryContent.value = '';
    // 获取传入文本中最大的字符占据的宽度。（注意：获取传入文本最大字符占据的宽度，应当是在value值存在的情况下）
    const maxCharWidth = await this.calculateMaxCharWidth(value);
    // 如果外层盒子的宽度没有达到一个字符占据的宽度组件不展示内容
    if (this.wrapperSize.width < maxCharWidth) {
      console.warn('设置的宽度太小');
      return;
    }
    // 计算行数
    const rows = Math.floor(Math.ceil(this.wrapperSize.height) / this.lineHeight);
    if (rows === 0) {
      console.warn('指定的高度太低无法显示');
      return;
    }
    if (isOverFlow) {
      const element = document.createElement('div');
      element.innerHTML = this.plainText;
      this.wrapperElement.firstElementChild!.nextElementSibling!.appendChild(element);
      element.parentElement!.style.overflow = 'hidden';
      // 获取...占据的宽度
      this.ellipsisWidth = await this.calculateEllipsisWidth();

      const contentArr = await this.generateVNodesInnerText(this.wrapperSize, value, rows);
      const wrapperNode = await this.generateVNode(contentArr, rows, this.lineHeight);
      this.temporaryContent.value = '';
      element.parentElement!.style.removeProperty('overflow');
      this.wrapperElement.firstElementChild!.nextElementSibling!.removeChild(element);
      // 将VNode渲染到页面
      render(wrapperNode, this.wrapperElement);
    } else {
      // 传入字符占据的宽度小于外层盒子的宽度，直接渲染
      const wrapperNode = await this.generateVNode([this.plainText], 1, this.lineHeight);
      render(wrapperNode, this.wrapperElement);
    }
  }

  private async clear(): Promise<void> {
    await this.init();
  }

  async reload(): Promise<void> {
    if (!this.transitCalculator) {
      return;
    }
    // 获取行高
    this.lineHeight = await this.calculateLineHeight();
    // 获取外层元素的宽高，如果外层元素未指定高度，默认将文字占据的行高值设置为高度。
    this.wrapperSize = this.calculateContainerSize(this.lineHeight);
    await this.clear();
    await this.init(this.plainText);
    const contentMaxWidth = await this.calculateContentMaxWidth();
    this.callback({ contentMaxWidth });
  }
}
