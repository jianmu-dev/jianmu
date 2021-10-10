<template>
  <router-view v-if="!loading"/>
  <div class="initialization" v-loading="loading"></div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onBeforeMount, ref } from 'vue';
import { mapActions } from 'vuex';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const loading = ref<boolean>(false);

    // 初始化app
    onBeforeMount(() => {
      loading.value = !loading.value;
      // 初始化vuex根状态
      proxy.initialize()
        .then(() => (loading.value = !loading.value))
        .catch((err: Error) => {
          loading.value = !loading.value;

          proxy.$throw(err, proxy);
        });
    });

    return {
      loading,
      ...mapActions(['initialize']),
    };
  },
});
</script>
<style scoped lang="less">
.initialization {
  position: fixed;
  width: 100vw;
  height: 100vh;
}
</style>