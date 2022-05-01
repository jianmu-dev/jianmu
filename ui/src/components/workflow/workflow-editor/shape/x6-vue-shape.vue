<template>
  <div :class="{ 'jm-workflow-x6-vue-shape': true, clickable }">
    <div class="icon"><img :src="iconUrl"/></div>
    <div class="name">{{ nameVal }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, PropType, ref } from 'vue';
import cronImg from '../svgs/shape/cron.svg';
import webhookImg from '../svgs/shape/webhook.svg';
import shellImg from '../svgs/shape/shell.svg';
import asyncTaskImg from '../svgs/shape/async-task.svg';
import { NodeTypeEnum } from '../model/enumeration';
import { INodeData } from '../model/data';

export default defineComponent({
  props: {
    nodeData: Object as PropType<INodeData>,
  },
  setup(props) {
    const iconUrl = ref<string>('');
    const nameVal = ref<string>('');

    onMounted(() => {
      // const getGraph = inject('getGraph') as () => Graph;
      const { type, icon, name } = props.nodeData || inject('getNode')().getData() as INodeData;
      switch (type) {
        case NodeTypeEnum.CRON:
          iconUrl.value = cronImg;
          break;
        case NodeTypeEnum.WEBHOOK:
          iconUrl.value = webhookImg;
          break;
        case NodeTypeEnum.SHELL:
          iconUrl.value = shellImg;
          break;
        case NodeTypeEnum.ASYNC_TASK:
          iconUrl.value = icon || asyncTaskImg;
          break;
      }
      nameVal.value = name;
      // // 监听数据改变事件
      // node.on('change:data', ({ current }) => {
      //   console.log('----,', current);
      // });
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
.jm-workflow-x6-vue-shape {
  width: 80px;

  &.clickable {
    cursor: pointer;
  }

  &:active {
    cursor: move;
  }

  .icon {
    width: 80px;
    height: 80px;

    img {
      width: 100%;
      height: 100%;
      border-radius: 25.5%;
      box-shadow: 0 0 8px 1px #C5D9FF;
    }
  }

  .name {
    max-height: 40px;
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