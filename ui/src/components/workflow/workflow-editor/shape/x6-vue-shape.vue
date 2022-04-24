<template>
  <div class="jm-workflow-x6-vue-shape">
    <div class="image"><img :src="imgUrl"/></div>
    <div class="text">{{ txt }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, ref } from 'vue';
import { Graph, Node } from '@antv/x6';
import cronImg from '../svgs/shape/cron.svg';
import webhookImg from '../svgs/shape/webhook.svg';
import shellImg from '../svgs/shape/shell.svg';
import asyncTaskImg from '../svgs/shape/async-task.svg';
import { NodeTypeEnum } from '../model/enumeration';

export default defineComponent({
  setup() {
    const imgUrl = ref<string>('');
    const txt = ref<string>('');
    const getGraph = inject('getGraph') as () => Graph;
    const getNode = inject('getNode') as () => Node;

    onMounted(() => {
      const node = getNode();
      const { nodeType, image, text } = node.getData();
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
      // 监听数据改变事件
      node.on('change:data', ({ current }) => {
        console.log('----,', current);
      });
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
  width: 80px;

  .image {
    width: 80px;
    height: 80px;

    img {
      width: 100%;
      height: 100%;
      border-radius: 25.5%;
    }
  }

  .text {
    font-size: 14px;
    text-align: center;
    // 英文单词换行
    word-wrap: break-word;
    // 中文换行
    white-space: pre-wrap;
  }
}
</style>