import { DebouncedFunc } from 'lodash';
import { ObjectDirective } from 'vue';
import _throttle from 'lodash/throttle';
export class ScrollableEl {
  private el: HTMLElement;
  private scroll: () => void;
  private timer: any;
  // 截流时间 单位ms
  private throttle: number | undefined;
  // 截流函数
  private throttleScroll: DebouncedFunc<() => void>;
  constructor(el: HTMLElement, loadMore: () => void, throttle?: number) {
    this.el = el;
    this.throttle = throttle;
    // 对滚动事件开启截流
    this.throttleScroll = _throttle(
      loadMore,
      this.throttle ? this.throttle : 0,
      {
        leading: true,
        trailing: false,
      },
    );
    this.scroll = () => {
      const { scrollTop, clientHeight, scrollHeight } = this.el;
      if (Math.floor(scrollTop) + clientHeight !== scrollHeight) {
        return;
      }
      this.throttleScroll();
    };
    this.el.addEventListener('scroll', this.scroll);
  }
  private getHeight(): number {
    const h =
      window.innerHeight ||
      document.documentElement.clientHeight ||
      document.body.clientHeight;
    let actualTop = this.el.offsetTop;
    let elementParent = this.el.offsetParent as HTMLElement;
    while (elementParent) {
      actualTop += elementParent.offsetTop;
      elementParent = elementParent.offsetParent as HTMLElement;
    }
    return h - actualTop;
  }
  /**
   * @param callback 指令真正触发的加载更多的函数
   */
  loadMore(callback: () => void) {
    if (this.timer) {
      clearTimeout(this.timer);
    }
    const { clientHeight, scrollHeight } = this.el;
    this.timer = setTimeout(() => {
      if (scrollHeight > this.getHeight() && scrollHeight > clientHeight) {
        return;
      }
      callback();
    }, 400);
  }

  destroy() {
    if (this.timer) {
      clearTimeout(this.timer);
    }
    this.el.removeEventListener('scroll', this.scroll);
    // 取消截流
    this.throttleScroll.cancel();
  }
}
export interface IBindingValue {
  scrollableEl: HTMLElement | (() => HTMLElement);
  loadMore: () => void;
  throttle?: number;
}
export type CustomObjectDirective = ObjectDirective & {
  el: ScrollableEl;
};
