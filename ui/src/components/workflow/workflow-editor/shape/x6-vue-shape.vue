<template>
  <div class="jm-workflow-x6-vue-shape">
    <div class="image"><img :src="imgUrl"/></div>
    <div class="text">{{ txt }}</div>
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
    const imgUrl = ref<string>('');
    const txt = ref<string>('');

    onMounted(() => {
      // const getGraph = inject('getGraph') as () => Graph;
      const { nodeType, image, text } = props.nodeData || inject('getNode')().getData() as INodeData;
      switch (nodeType) {
        case NodeTypeEnum.CRON:
          imgUrl.value = cronImg;
          break;
        case NodeTypeEnum.WEBHOOK:
          imgUrl.value = webhookImg;
          break;
        case NodeTypeEnum.SHELL:
          imgUrl.value = shellImg;
          break;
        case NodeTypeEnum.ASYNC_TASK:
          imgUrl.value = image || asyncTaskImg;
          break;
      }
      txt.value = text;
      // // 监听数据改变事件
      // node.on('change:data', ({ current }) => {
      //   console.log('----,', current);
      // });
    });

    return {
      imgUrl,
      txt,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-x6-vue-shape {
  cursor: pointer;
  width: 80px;

  &:active {
    cursor: move;
  }

  .image {
    width: 80px;
    height: 80px;

    img {
      width: 100%;
      height: 100%;
      border-radius: 25.5%;
      box-shadow: 0 0 8px 1px #C5D9FF;
    }
  }

  .text {
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