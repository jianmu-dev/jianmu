<template>
  <div class="form-tags-input">
    <div>tags输入框：</div>
    <div>
      <jm-tags-input
        placeholder="请输入command"
        v-model="value"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  setup() {
    return {
      value: ref(['a1', 'a2', 'a3']),
    };
  },
});
</script>

<style scoped lang="less">
.form-tags-input {

}
</style>