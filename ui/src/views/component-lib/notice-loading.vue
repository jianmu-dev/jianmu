<template>
  <div class="notice-loading">
    加载：
    <jm-button @click="loading()">加载中</jm-button>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance } from 'vue';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;

    return {
      loading() {
        const loadingInstance = proxy.$loading({
          text: '3秒后，关闭loading',
        });

        setTimeout(() => loadingInstance.close(), 3000);
      },
    };
  },
});
</script>

<style scoped lang="less">
.notice-loading {

}
</style>