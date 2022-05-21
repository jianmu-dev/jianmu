<template>
  <div class="full">
    <router-view v-if="loadMain"></router-view>
  </div>
</template>

<script lang='ts'>
import { defineComponent, getCurrentInstance, provide, ref } from 'vue';

export default defineComponent({
  name: 'full',
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const loadMain = ref<boolean>(true);
    const reloadMain = () => {
      loadMain.value = false;
      proxy.$nextTick(() => (loadMain.value = true));
    };
    provide('reloadMain', reloadMain);
    return {
      loadMain,
    };
  },
});
</script>

<style scoped lang='less'>
</style>
