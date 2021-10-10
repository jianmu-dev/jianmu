<template>
  <div class="data-date-picker">
    日期选择器：
    <jm-date-picker v-model="date"
                    type="date"
                    placeholder="选择日期"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs } from 'vue';

export default defineComponent({
  setup() {
    const state: {
      date: Date | undefined;
    } = reactive({ date: undefined });

    return { ...toRefs(state) };
  },
});
</script>

<style scoped lang="less">
.data-date-picker {

}
</style>