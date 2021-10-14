<template>
  <div class="data-pagination">
    <div>分页：</div>
    <jm-pagination
      :current-page="5"
      :total="400"/>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.data-pagination {
  display: flex;
  align-items: center;
}
</style>