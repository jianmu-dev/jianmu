import { createVNode, render, Component, ref } from 'vue';
import type { AppContext } from 'vue';

/**
 * 动态渲染组件
 * @param component
 * @param appContext
 * @param props
 */
export default (
  component: Component,
  appContext: AppContext,
  props?: Record<string, unknown>,
) => {
  // 定义一个变量保存HTML元素
  const htmlElementObj = ref<HTMLElement>();
  // 定义一个销毁方法，判断HTML元素变量中是否有值，没有值则销毁
  const destroy = () => {
    // 如果没有值就直接退出这个方法
    if (!htmlElementObj.value) {
      return;
    }
    // 如果有值就移除节点
    document.body.removeChild(htmlElementObj.value);
  };
  // 创建根节点
  const container = document.createElement('div');
  // 创建虚拟节点
  const vNode = createVNode(component, {
    ...props,
    onVnodeUnmounted: () => {
      destroy();
    },
  });
  vNode.appContext = appContext;
  // 通过render渲染虚拟节点到根节点中
  render(vNode, container);
  // 将创建的虚拟节点保存到定义的变量中
  htmlElementObj.value = container.firstElementChild as HTMLElement;
  // 向body追加获取的虚拟节点
  document.body.appendChild(htmlElementObj.value);
};
