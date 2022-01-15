import { createVNode, nextTick, render, Ref, AppContext, VNode, VNodeProps } from 'vue';
import Tip from './tip.vue';

export interface ICallbackEvent {
  contentMaxWidth: number
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
    this.resizeObserver = new ResizeObserver(() => this.reload());
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
    const maxWidth = Math.max(w, this.transitCalculator.value!.offsetWidth);
    if (i === value.length - 1) {
      this.temporaryContent.value = '';
      return maxWidth;
    }
    return await this.calculateMaxCharWidth(value, i + 1, maxWidth);
  }

  private async calculateContentMaxWidth(value: string = this.value.value): Promise<number> {
    this.temporaryContent.value=value;
    await nextTick();
    if(!this.transitCalculator.value||!this.transitCalculator.value.getClientRects()[0]){
      return 0;
    }
    // 需要用getClientRects去获取元素的宽度精确到小数点后三位
    const maxWidth=this.transitCalculator.value?.getClientRects()[0].width;
    this.temporaryContent.value = '';
    return Math.ceil( maxWidth);
  }

  private async calculateEllipsisWidth(): Promise<number> {
    this.temporaryContent.value = '...';
    await nextTick();
    const ellipsisWidth = this.transitCalculator.value!.offsetWidth;
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
    if (!this.transitCalculator.value || !this.transitCalculator.value?.getClientRects()[0]) {
      return 0;
    }
    return Math.floor(this.transitCalculator.value!.getClientRects()[0].height);
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
    const { clientWidth, clientHeight } = this.transitCalculator.value!.parentElement as HTMLElement;
    return {
      width: clientWidth,
      height: clientHeight || lineHeight,
    };
  }

  /**
   * 生成元素对象
   * @param rows 行数
   * @param lineHeight 行高
   * @return 对象数组
   */
  private generateElements(rows: number, lineHeight: number): (Record<string, unknown> & VNodeProps)[] {
    if (!rows) {
      return [];
    }
    return new Array(rows).fill('').map(() => ({
      style: {
        height: `${lineHeight}px`,
      },
      class: 'text-line',
    }));
  }

  /**
   * 生成vNode中的文本
   * @param wrapperSize 容器大小
   * @param value 文本内容
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
    i: number = 0,
    contentArr: string[] = [],
  ): Promise<string[]> {
    this.temporaryContent.value += value[i];
    await nextTick();
    if (!this.transitCalculator.value) {
      return [];
    }
    // 截取到字符串的offsetWidth大于外层元素设置的宽度终止递归
    if (this.transitCalculator.value!.offsetWidth > wrapperSize.width) {
      contentArr.push(this.temporaryContent.value?.substring(0, i));
      // 文本内容截取后将响应式占位宽度的span内容清空
      this.temporaryContent.value = '';
      return await this.generateVNodesInnerText(wrapperSize, value, i, contentArr);
    }
    // 截取到字符串最后一位递归结束
    if (i === value.length - 1) {
      // 文本内容截取后将响应式占位宽度的span内容清空
      this.temporaryContent.value = '';
      return contentArr;
    }
    return await this.generateVNodesInnerText(wrapperSize, value, i + 1, contentArr);
  }

  /**
   * 生成最终需要渲染到页面的VNode
   * @param elements vNode元素描述的数组
   * @param contentArr vNode中的文本内容数组
   * @param rows 行数
   * @param lineHeight 行高
   * @param wrapperSize 容器大小
   * @param value 文本内容
   * @return vNode
   */
  private async generateVNode(
    elements: (Record<string, unknown> & VNodeProps)[],
    contentArr: string[],
    rows: number,
    lineHeight: number,
    wrapperSize: {
      readonly width: number;
      readonly height: number;
    },
    value: string,
  ): Promise<VNode> {
    const vNodes: VNode[] = [];
    // 当前str截取到的索引位置
    for (let i = 0; i < rows; i++) {
      // 如果数组中没有内容证明没有达到指定宽度，不做操作
      if (contentArr.length === 0) {
        break;
      }
      if (!contentArr[i]) {
        continue;
      }
      let vNode: VNode;
      if (i === 0) {
        vNode = createVNode('div', elements[i], contentArr[i]);
      } else {
        vNode = createVNode('div', elements[i], contentArr[i].substring(0, contentArr[i].length - 1));
      }
      // 循环向vNode中追加文本，只有vNode中存在内容时才添加，减少没有意义的vNode
      vNodes.push(vNode);
    }
    // 传入字符串的长度小于等于外层元素限制的宽度
    if (contentArr.length === 0 && value !== '') {
      const vNode = createVNode('div', { style: { height: `${lineHeight}px` }, class: 'text-line' }, value);
      vNodes.push(vNode);
      return createVNode('div', { class: 'content' }, vNodes);
    }
    // 如果行号大于数组的长度说明设置高度大于整体文字内容占据的高度，将内容整体显示
    if ((rows > contentArr.length) && contentArr.length !== 0) {
      elements.push({
        style: {
          height: `${lineHeight}px`,
        },
        class: 'text-line',
      });
      // 目前剩余字符开始索引
      const remainingCharacterIndex = contentArr.reduce((a, _, i, arr) => {
        return a + arr[i].length;
      }, 0) - (contentArr.length - 1);
      const vNode = createVNode('div', elements[elements.length - 1], value.substring(remainingCharacterIndex, value.length));
      vNodes.push(vNode);
      return createVNode('div', { class: 'content' }, vNodes);
    }
    if (rows > 0) {
      // 创建tooltip的VNode
      const toolTipVNode = createVNode(Tip, {
        effect: 'dark', content: value, placement: this.tipPlacement, style: { cursor: 'default' },
      }
      , null);
      toolTipVNode.appContext = this.appContext;
      // 更改最后一行的样式
      Object.assign(elements[rows - 1].style, { width: wrapperSize.width });
      elements[rows - 1].class = 'text-line last';
      vNodes[vNodes.length - 1] = createVNode('div', {
        class: 'text-line last',
        style: {
          height: `${lineHeight}px`,
        },
      }, [(vNodes[vNodes.length - 1] as VNode).children, toolTipVNode]);
    }
    return createVNode('div', { class: 'content' }, vNodes);
  }

  /**
   * 对最后一个VNode中的溢出文本的处理
   * @param wrapperNode 最终会被渲染到页面的vNode节点
   * @param wrapperSize 容器大小
   */
  private async handleOverFlowText(wrapperNode: any, wrapperSize: any) {
    if (!this.transitCalculator.value) {
      return;
    }
    // 将中转计算元素的盒模型改成inline用来计算文字宽度
    this.transitCalculator.value!.style.display = 'inline';
    // 获取...占据的宽度
    const ellipsisWidth = await this.calculateEllipsisWidth();
    // 递归出加上...占据的宽度后应该展示的vNode中的文本内容
    if (wrapperNode.children[wrapperNode.children.length - 1].children.length <= 1) {
      return;
    }
    this.temporaryContent.value += wrapperNode.children[wrapperNode.children.length - 1].children[0];
    await nextTick();
    if (this.transitCalculator.value!.offsetWidth + ellipsisWidth > wrapperSize.width) {
      const temp = wrapperNode.children[wrapperNode.children.length - 1].children[0];
      // 如果vNode中的children不是数组（数组证明此时vNode中有toolTipVNode），说明没有达到出...的条件，直接将文字展示不做处理
      if(!Array.isArray(wrapperNode.children[wrapperNode.children.length - 1].children)){
        return;
      }
      wrapperNode.children[wrapperNode.children.length - 1].children[0] = '';
      wrapperNode.children[wrapperNode.children.length - 1].children[0] += temp.substring(0, temp.length - 1);
    } else {
      return;
    }
    await this.handleOverFlowText(wrapperNode, wrapperSize);
  }

  private async init(value: string = '') {
    if (!this.transitCalculator.value) {
      return;
    }
    const lineHeight = await this.calculateLineHeight();
    const wrapperSize = this.calculateContainerSize(lineHeight);
    if (value.length === 0) {
      const wrapperNode = await this.generateVNode([], [], 0, lineHeight, wrapperSize, value);
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
    const elements = this.generateElements(rows, lineHeight);
    const contentArr = await this.generateVNodesInnerText(wrapperSize, value);
    const wrapperNode = await this.generateVNode(elements, contentArr, rows, lineHeight, wrapperSize, value);
    await this.handleOverFlowText(wrapperNode, wrapperSize);
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
    const contentMaxWidth = await this.calculateContentMaxWidth();
    this.callback({ contentMaxWidth });
  }
}
