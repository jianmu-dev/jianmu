import { createVNode, nextTick, render, Ref, AppContext, VNode, VNodeProps } from 'vue';
import Tip from './tip.vue';
import TextLine from './text-line.vue';

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
    this.resizeObserver = new ResizeObserver(async () => {
      await this.reload();
    });
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
    this.temporaryContent.value += value[i];
    await nextTick();
    if (!this.transitCalculator.value) {
      return [];
    }
    let contentWidth = this.transitCalculator.value?.getBoundingClientRect().width;
    if (rows === 1 || rows === contentArr.length + 1) {
      contentWidth += this.ellipsisWidth;
    }
    // 截取到字符串的offsetWidth大于外层元素设置的宽度终止递归
    if (contentWidth > wrapperSize.width) {
      contentArr.push(this.temporaryContent.value.substring(0, this.temporaryContent.value.length - 1));
      // 文本内容截取后将响应式占位宽度的span内容清空
      this.temporaryContent.value = '';
      // 生成多行的VNode中的文本
      if (rows === 1 || rows === contentArr.length) {
        return contentArr;
      }
      return await this.generateVNodesInnerText(wrapperSize, value, rows, i, contentArr);
    }
    // 截取到字符串最后一位递归结束
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
    // 当前str截取到的索引位置
    for (let i = 0; i < rows; i++) {
      // 内容生成的数组长度小于指定的rows行数
      if (!contentArr[i]) {
        break;
      }
      const textLineVNode = createVNode(TextLine, { value: contentArr[i], style: { height: `${lineHeight}px` } });
      if (i < Math.min(contentArr.length, rows) - 1) {
        // 循环向vNode中追加文本，只有vNode中存在内容时才添加，减少没有意义的vNode
        vNodes.push(textLineVNode);
        continue;
      }
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
    this.ellipsisWidth = await this.calculateEllipsisWidth();
    const lineHeight = await this.calculateLineHeight();
    const wrapperSize = this.calculateContainerSize(lineHeight);
    if (value.length === 0) {
      const wrapperNode = await this.generateVNode([], 0, lineHeight, wrapperSize, value);
      render(wrapperNode, this.transitCalculator.value!.parentElement as HTMLElement);
      return;
    }
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
    render(wrapperNode, this.transitCalculator.value!.parentElement as HTMLElement);
  }

  private async clear() {
    await this.init();
  }

  async reload() {
    await nextTick();
    await this.clear();
    await nextTick();
    await this.init(this.value.value);
    const contentMaxWidth = await this.calculateContentMaxWidth() + this.ellipsisWidth;
    this.callback({ contentMaxWidth });
  }
}
