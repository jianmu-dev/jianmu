import { createVNode, nextTick, render, Ref, AppContext, VNode } from 'vue';
import Tip from './tip.vue';
import TextLine from './text-line.vue';
import _throttle from 'lodash/throttle';
export interface ICallbackEvent {
  contentMaxWidth: number;
}

type Callback = (event: ICallbackEvent) => void

export class TextViewer {
  private readonly value: Ref<string>;
  private readonly tipPlacement: string;
  // 解决element组件未被注册的警告提示
  private readonly appContext: AppContext;
  private readonly temporaryContent: Ref<string>;
  // 中转计算元素
  private readonly transitCalculator: Ref<HTMLElement | undefined>;
  // 事件监听器
  private readonly resizeObserver: ResizeObserver;

  private readonly callback: Callback;
  // 身略号占据的宽度
  private ellipsisWidth: number = 0;

  constructor(
    value: Ref<string>,
    tipPlacement: string,
    temporaryContent: Ref<string>,
    transitCalculator: Ref<HTMLElement | undefined>,
    appContext: AppContext,
    callback: Callback,
  ) {
    this.value = value;
    this.temporaryContent = temporaryContent;
    this.transitCalculator = transitCalculator;
    this.appContext = appContext;
    this.tipPlacement = tipPlacement;
    this.callback = callback;
    this.resizeObserver = new ResizeObserver(_throttle(async () => {
      await this.reload();
    }, 800));
    this.resizeObserver.observe(this.transitCalculator.value!.parentElement!.parentElement as Element);
  }

  /**
   * 计算单个文字最大宽度
   * @param value
   * @param i
   * @param w
   * @return 最大宽度
   */
  private async calculateMaxCharWidth(value: string, i: number = 0, w: number = 0): Promise<number> {
    this.temporaryContent.value = value[i];
    await nextTick();
    if (!this.transitCalculator.value) {
      return 0;
    }
    const maxWidth = Math.max(w, this.transitCalculator.value.getBoundingClientRect().width);
    // 比较到字符内容的最后一位的字符得到最大的宽度值，结束递归。
    if (i === value.length - 1) {
      this.temporaryContent.value = '';
      return maxWidth;
    }
    return await this.calculateMaxCharWidth(value, i + 1, maxWidth);
  }

  /**
   * 计算出入内容会占据的最大宽度
   * @param value
   * @return 传入文本在页面会占据的最大宽度
   */
  private async calculateContentMaxWidth(value: string = this.value.value): Promise<number> {
    this.temporaryContent.value = value;
    await nextTick();
    if (!this.transitCalculator.value || !this.transitCalculator.value.getBoundingClientRect()) {
      return 0;
    }
    // 需要用getClientRects去获取元素的宽度精确到小数点后三位
    const maxWidth = this.transitCalculator.value.getBoundingClientRect().width;
    this.temporaryContent.value = '';
    return Math.ceil(maxWidth);
  }

  private async calculateEllipsisWidth(): Promise<number> {
    this.temporaryContent.value = '...';
    await nextTick();
    const ellipsisWidth = this.transitCalculator.value!.getBoundingClientRect().width;
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
    if (!this.transitCalculator.value || !this.transitCalculator.value.getBoundingClientRect()) {
      return 0;
    }
    return Math.floor(this.transitCalculator.value!.getBoundingClientRect().height);
  }

  /**
   * 计算容器大小
   * @param lineHeight 行高
   * @return 容器大小
   */
  private calculateContainerSize(lineHeight: number): {
    readonly width: number;
    readonly height: number;
  } {
    if (!this.transitCalculator.value) {
      return {
        width: 0,
        height: 0,
      };
    }
    const { clientWidth, clientHeight } = this.transitCalculator.value.parentElement as HTMLElement;
    return {
      width: clientWidth,
      height: clientHeight || lineHeight,
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
    wrapperSize: {
      readonly width: number;
      readonly height: number;
    },
    value: string,
    rows: number,
    i: number = 0,
    contentArr: string[] = [],
  ): Promise<string[]> {
    if (!this.transitCalculator.value) {
      return [];
    }
    this.temporaryContent.value += value[i];
    await nextTick();
    // 获取到此时用于计算的元素中的文字在页面中占据的实际宽度
    let contentWidth = this.transitCalculator.value.getBoundingClientRect().width;
    // 提前先判断最后剩余的文本内容占据的宽度是否会大于外层容器的宽度，若大于外层容器的宽度，控制只让最后一行文本是带着...去计算生成截取的文本
    if (rows === contentArr.length + 1 && await this.calculateLastLineMaxWidth(contentArr) > wrapperSize.width) {
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
   * @param value
   * @return 最后一行文本内容占据的最大宽度
   */
  private async calculateLastLineMaxWidth(contentArr: string[], value: string = this.value.value): Promise<number> {
    // 将此时用于占据计算元素中的值当成临时变量保存。
    const temp = this.temporaryContent.value;
    // 获取此时保存在文本数组中的所有文本长度
    const contentLength = contentArr.reduce((pre, current) => {
      return pre + current.length;
    }, 0);
    // 得到最后一行中的文本内容
    const lastLineValue = value.substring(contentLength);
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
   * @param wrapperSize 容器大小
   * @param value 文本内容
   * @return vNode
   */
  private async generateVNode(
    contentArr: string[],
    rows: number,
    lineHeight: number,
    wrapperSize: {
      readonly width: number;
      readonly height: number;
    },
    value: string,
  ): Promise<VNode> {
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
      const textLineVNode = createVNode(TextLine, { value: contentArr[i], style: { height: `${lineHeight}px` } });
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
      if (contentLength === this.value.value.length) {
        vNodes.push(textLineVNode);
        continue;
      }
      // 创建tooltip的VNode
      const toolTipVNode = createVNode(Tip, {
        effect: 'dark', content: value, placement: this.tipPlacement, style: { cursor: 'default' },
      }, null);
      toolTipVNode.appContext = this.appContext;
      // 创建出...的vNode
      vNodes.push(createVNode('span', {
        class: 'text-line-last',
        style: { height: `${lineHeight}px` },
      }, [textLineVNode, toolTipVNode]));
    }
    return createVNode('div', { class: 'content' }, vNodes);
  }

  private async init(value: string = '') {
    if (!this.transitCalculator.value) {
      return;
    }
    // 获取行高
    const lineHeight = await this.calculateLineHeight();
    // 获取外层元素的宽高，如果外层元素未指定高度，默认将文字占据的行高值设置为高度。
    const wrapperSize = this.calculateContainerSize(lineHeight);
    let isOverFlow:boolean=false;
    this.temporaryContent.value=this.value.value;
    await nextTick();
    isOverFlow=this.transitCalculator.value!.getBoundingClientRect().width>wrapperSize.width;
    this.temporaryContent.value='';
    if (value.length === 0) {
      // 如果传入的内容为空字符串，生成一个类名为content的空div 作为VNode
      const wrapperNode = await this.generateVNode([], 0, lineHeight, wrapperSize, value);
      render(wrapperNode, this.transitCalculator.value!.parentElement as HTMLElement);
      return;
    }
    if(isOverFlow){
      const element=document.createElement('div');
      element.innerHTML=this.value.value;
      this.transitCalculator.value.nextElementSibling!.appendChild(element);
      element.parentElement!.style.overflow='hidden';
      await nextTick();
      element.parentElement!.style.removeProperty('overflow');
      // 获取...占据的宽度
      this.ellipsisWidth = await this.calculateEllipsisWidth();
      // 获取传入文本中最大的字符占据的宽度。
      const maxCharWidth = await this.calculateMaxCharWidth(value);
      // 如果外层盒子的宽度没有达到一个字符占据的宽度组件不展示内容
      if (wrapperSize.width < maxCharWidth) {
        console.warn('设置的宽度太小');
        return;
      }
      // 计算行数
      const rows = Math.floor(wrapperSize.height / lineHeight);
      if (rows === 0) {
        console.warn('指定的高度太低无法显示');
        return;
      }
      const contentArr = await this.generateVNodesInnerText(wrapperSize, value, rows);
      const wrapperNode = await this.generateVNode(contentArr, rows, lineHeight, wrapperSize, value);
      this.temporaryContent.value = '';
      // 将VNode渲染到页面
      render(wrapperNode, this.transitCalculator.value.parentElement as HTMLElement);
      this.transitCalculator.value.nextElementSibling!.removeChild(element);
    }else{
      // 传入字符占据的宽度小于外层盒子的宽度，直接渲染
      const wrapperNode = await this.generateVNode([this.value.value], 1, lineHeight, wrapperSize, this.value.value);
      render(wrapperNode, this.transitCalculator.value!.parentElement as HTMLElement);
    }
  }

  private async clear() {
    await this.init();
  }

  async reload() {
    await nextTick();
    await this.clear();
    await nextTick();
    await this.init(this.value.value);
    const contentMaxWidth = await this.calculateContentMaxWidth();
    this.callback({ contentMaxWidth });
  }
}
