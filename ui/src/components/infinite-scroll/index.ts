import type { App, DirectiveBinding } from 'vue';
import { CustomObjectDirective, ScrollableEl, IBindingValue } from './model';
// 指令
const vScroll = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { scrollableEl, loadMore, throttle } = binding.value as IBindingValue;
    const dir = binding.dir as CustomObjectDirective;
    const element = scrollableEl
      ? typeof scrollableEl === 'function'
        ? scrollableEl()
        : scrollableEl
      : el;
    dir.el = new ScrollableEl(element, loadMore, throttle);
  },
  updated(_: HTMLElement, binding: DirectiveBinding) {
    const { el } = binding.dir as CustomObjectDirective;
    const { loadMore } = binding.value as IBindingValue;
    el.loadMore(loadMore);
  },
  beforeUnmount(_: HTMLElement, binding: DirectiveBinding) {
    const { el } = binding.dir as CustomObjectDirective;
    el.destroy();
  },
};
export default {
  install(app: App) {
    app.directive('scroll', vScroll);
  },
  directive: vScroll,
};
