<template>
  <div class="form-radio">
    单选框：
    <jm-radio v-model="radio" :label="1">选项1</jm-radio>
    <jm-radio v-model="radio" :label="2">选项2</jm-radio>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs } from 'vue';

export default defineComponent({
  setup() {
    const state: {
      radio: number;
    } = reactive({ radio: 1 });

    return { ...toRefs(state) };
  },
});
</script>

<style scoped lang="less">
.form-radio {

}
</style>