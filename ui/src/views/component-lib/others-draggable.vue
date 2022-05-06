<template>
  <div class="other-draggable">
    拖拽排序：
    <jm-sorter class="list" v-model="list" @change="log">
      <div v-for="item in list" :key="item.id" class="item">
        {{ item.name }}
      </div>
    </jm-sorter>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  setup() {
    return {
      list: ref<object[]>([
        { name: 'First', id: 1 },
        { name: 'Second', id: 2 },
        { name: 'Third', id: 3 },
        { name: 'Fourth', id: 4 },
      ]),
      log: (event: any) => {
        console.log(event);
      },
    };
  },
});
</script>

<style scoped lang="less">
.list {
  .item {
    margin: 0 10px;
  }
}
</style>
