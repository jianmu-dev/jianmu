import { ElContainer } from 'element-plus';
import { Component, computed, VNode } from 'vue';

export default {
  ...ElContainer,
  name: 'jm-container',
  setup(props: any, { slots }: { slots: any; }) {
    const isVertical = computed(() => {
      if (props.direction === 'vertical') {
        return true;
      } else if (props.direction === 'horizontal') {
        return false;
      }
      if (slots && slots.default) {
        const vNodes: VNode[] = slots.default();
        return vNodes.some(vNode => {
          const tag = (vNode.type as Component).name;
          return tag === 'jm-header' || tag === 'jm-footer';
        });
      } else {
        return false;
      }
    });
    return {
      isVertical,
    };
  },
};