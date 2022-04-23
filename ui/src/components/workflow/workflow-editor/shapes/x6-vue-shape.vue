<template>
  <div class="jm-workflow-x6-vue-shape">
    <img :src="imgUrl" width="80" height="80"/>
    <jm-text-viewer :value="txt"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, ref } from 'vue';
import { Graph, Node } from '@antv/x6';
import JmTextViewer from '@/components/text-viewer/index.vue';

export default defineComponent({
  components: { JmTextViewer },
  setup() {
    const imgUrl = ref<string>('');
    const txt = ref<string>('');
    const getGraph = inject('getGraph') as () => Graph;
    const getNode = inject('getNode') as () => Node;

    onMounted(() => {
      const node = getNode();
      const { image, text } = node.getData();
      imgUrl.value = image;
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
  height: 80px;

  img {
    border-radius: 25.5%;
  }
}
</style>