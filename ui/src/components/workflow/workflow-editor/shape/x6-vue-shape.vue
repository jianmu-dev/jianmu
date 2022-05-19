<template>
  <div ref="shape" :class="{ 'jm-workflow-x6-vue-shape': true, clickable }">
    <div class="icon">
      <div class="img" :style="`background-image: url('${iconUrl}');`"></div>
    </div>
    <div class="name">{{ nameVal }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, PropType, ref } from 'vue';
import { Node } from '@antv/x6';
import { IWorkflowNode } from '../model/data/common';
import { CustomX6NodeProxy } from '../model/data/custom-x6-node-proxy';

export default defineComponent({
  props: {
    nodeData: Object as PropType<IWorkflowNode>,
  },
  setup(props) {
    const shape = ref<HTMLElement>();
    const iconUrl = ref<string>('');
    const nameVal = ref<string>('');

    const refresh = (data: IWorkflowNode) => {
      iconUrl.value = data.getIcon();
      nameVal.value = data.getName();
    };

    onMounted(() => {
      // const getGraph = inject('getGraph') as () => Graph;
      let data = props.nodeData;

      if (!data) {
        const node = (inject('getNode') as () => Node)();
        const proxy = new CustomX6NodeProxy(node);
        // 监听数据改变事件
        node.on('change:data', () => refresh(proxy.getData()));

        data = proxy.getData();

        shape.value!.setAttribute('data-x6-node-id', node.id);
      }

      refresh(data);
    });

    return {
      shape,
      clickable: !props.nodeData,
      iconUrl,
      nameVal,
    };
  },
});
</script>

<style scoped lang="less">
@import '../vars';

.jm-workflow-x6-vue-shape {
  width: @node-icon-width;

  &:hover {
    .icon {
      .img {
        //box-shadow: 0 0 10px 4px #C6D3DF;
        box-shadow: 0 0 8px 1px #C5D9FF;
      }
    }
  }

  &.clickable {
    cursor: pointer;
  }

  &:active {
    cursor: move;
  }

  .icon {
    width: @node-icon-width;
    height: @node-icon-height;
    margin-bottom: @node-icon-margin-bottom;
    border-radius: @icon-border-radius;

    .img {
      width: 100%;
      height: 100%;
      border-radius: @icon-border-radius;
      background-size: cover;
    }
  }

  .name {
    margin-top: 6px;
    max-height: @node-text-max-height;
    line-height: 20px;
    // 超过两行直接截断
    overflow: hidden;
    font-size: 14px;
    text-align: center;
    // 英文单词换行
    word-wrap: break-word;
    // 中文换行
    white-space: pre-wrap;
    color: @label-color;
  }
}
</style>