<template>
  <div class="form-checkbox">
    复选框：
    <jm-checkbox v-model="checked">选项</jm-checkbox>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs } from 'vue';

export default defineComponent({
  setup() {
    const state: {
      checked: boolean;
    } = reactive({ checked: false });

    return { ...toRefs(state) };
  },
});
</script>

<style scoped lang="less">
.form-checkbox {

}
</style>