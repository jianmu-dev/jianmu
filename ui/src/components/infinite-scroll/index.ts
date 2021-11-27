import type { App, DirectiveBinding } from 'vue';
import { nextTick } from 'vue';
import _throttle from 'lodash/throttle';
import { CustomObjectDirective } from './model';
const findRootElement = (el: HTMLElement | null): HTMLElement | null => {
  // 递归发现没父元素直接结束
  if (!el) {
    return null;
  }
  if (el.parentElement && el.parentElement.tagName === 'BODY') {
    return el;
  }
  return findRootElement(el.parentElement);
};
// 指令
const vScroll = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    let rootElement = findRootElement(el);
    const dir = binding.dir as CustomObjectDirective;
    // 默认设置挂载元素高度
    if (binding.modifiers.current) {
      rootElement = el;
      nextTick(() => {
        // 浏览器计算出来的高度为0，提示警告
        if (rootElement!.clientHeight === 0) {
          console.warn('无法拿到元素初始高度，请设置元素高度');
          return;
        }
      });
    } else {
      if (!rootElement) {
        return;
      }
      rootElement.style.height = '100vh';
    }
    rootElement.style.overflowY = 'auto';
    dir.throttleScroll = _throttle(binding.value, 800, {
      leading: true,
      trailing: false,
    });
    dir.handler = function () {
      // 滚动触底
      if (
        !rootElement ||
        rootElement.scrollTop + rootElement.clientHeight ===
          rootElement.scrollHeight
      ) {
        dir.throttleScroll();
      }
    };
    rootElement.addEventListener('scroll', dir.handler);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    let rootElement = findRootElement(el);
    if (binding.modifiers.current) {
      rootElement = el;
      const dir = binding.dir as CustomObjectDirective;
      if (dir.timer) {
        clearTimeout(dir.timer);
      }
      dir.timer = setTimeout(() => {
        if (rootElement!.scrollHeight > rootElement!.clientHeight) {
          return;
        }
        binding.value();
      }, 400);
    }
  },
  beforeUnmount(el: HTMLElement, binding: DirectiveBinding) {
    let rootElement = findRootElement(el);
    if (binding.modifiers.current) {
      rootElement = el;
    }
    if (!rootElement) {
      return;
    }
    const { handler, timer, throttleScroll } =
      binding.dir as CustomObjectDirective;
    if (timer) {
      clearTimeout(timer);
    }
    rootElement.removeEventListener('scroll', handler);
    // 取消截流
    throttleScroll.cancel();
  },
};
export default {
  install(app: App) {
    app.directive('scroll', vScroll);
  },
  directive: vScroll,
};
