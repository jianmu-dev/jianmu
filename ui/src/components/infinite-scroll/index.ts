import type { App, DirectiveBinding, ObjectDirective } from 'vue';
// 查找指令挂载的根节点
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
function getStyle(element: HTMLElement, attr: string) {
  // @ts-ignore
  if (element.currentStyle) {
    // @ts-ignore
    return element.currentStyle[attr];
  } else {
    // @ts-ignore
    return getComputedStyle(element, null)[attr];
  }
}
// 指令
const vScroll = {
  // 挂载指令
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    let rootElement = findRootElement(el);
    // 默认设置挂载元素高度
    if (binding.modifiers.current) {
      rootElement = el;
      // 为找到挂载指令的元素
      if (!rootElement) {
        return;
      }
      // rootElement指定为当前节点，设置height直接为浏览器computed自动计算的值
      const height = getStyle(el, 'height');
      // 浏览器计算出来的高度为0，提示警告
      if (!height) {
        console.warn('无法拿到元素初始高度，请设置元素高度');
        return;
      }
      rootElement.style.height = getStyle(el, 'height');
    } else {
      if (!rootElement) {
        return;
      }
      rootElement.style.height = '100vh';
    }
    rootElement.style.overflowY = 'auto';
    const dir = binding.dir as ObjectDirective & { handler: () => void };
    dir.handler = function () {
      if (
        !rootElement ||
        rootElement.scrollTop + rootElement.clientHeight !==
          rootElement.scrollHeight
      ) {
        return;
      }
      // 滚动触底
      binding.value();
    };
    rootElement.addEventListener('scroll', dir.handler);
  },
  // 卸载指令
  unmounted(el: HTMLElement, binding: DirectiveBinding) {
    let rootElement = findRootElement(el);
    if (binding.modifiers.current) {
      rootElement = el;
    }
    if (!rootElement) {
      return;
    }
    const { handler } = binding.dir as ObjectDirective & {
      handler: () => void;
    };
    rootElement.removeEventListener('scroll', handler);
  },
};
export default {
  install(app: App) {
    app.directive('scroll', vScroll);
  },
  directive: vScroll,
};
