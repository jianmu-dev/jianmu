<template>
  <div class="jm-workflow-x6-vue-shape">
    <div :class="{ 'x6-vue-shape-icon': true, clickable }">
      <div class="img" :style="`background-image: url('${iconUrl}');`"></div>
    </div>
    <div class="x6-vue-shape-name">{{ nameVal }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, PropType, ref } from 'vue';
import { IWorkflowNode } from '../model/data/common';
import { BaseNode } from '../model/data/node/base-node';
import { CustomX6NodeProxy } from '../model/data/custom-x6-node-proxy';

export default defineComponent({
  props: {
    nodeData: Object as PropType<IWorkflowNode>,
  },
  setup(props) {
    const iconUrl = ref<string>('');
    const nameVal = ref<string>('');

    const refresh = ({ icon, name }: BaseNode) => {
      iconUrl.value = icon;
      nameVal.value = name;
    };

    onMounted(() => {
      // const getGraph = inject('getGraph') as () => Graph;
      let data = props.nodeData;

      if (!data) {
        const node = inject('getNode')();
        const proxy = new CustomX6NodeProxy(node);
        // 监听数据改变事件
        node.on('change:data', () => refresh(proxy.getData()));

        data = proxy.getData();
      }

      refresh(data);
    });

    return {
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

  .x6-vue-shape-icon {
    width: @node-icon-width;
    height: @node-icon-height;

    &:hover {
      .img {
        //box-shadow: 0 0 10px 4px #C6D3DF;
        box-shadow: 0 0 8px 1px #C5D9FF;
      }
    }

    &.clickable {
      cursor: pointer;
    }

    &:active {
      cursor: move;
    }

    .img {
      width: 100%;
      height: 100%;
      border-radius: 25.5%;
      background-size: cover;
    }
  }

  .x6-vue-shape-name {
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
  }
}
</style>