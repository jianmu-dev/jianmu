<template>
  <div class="other-draggable">
    拖拽排序：
    <jm-draggable class="list" v-model="list" @change="log">
      <transition-group type="transition" name="flip-list">
        <div v-for="item in list" :key="item.id">
          {{ item.name }}
        </div>
      </transition-group>
    </jm-draggable>
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
.other-draggable {
  .list {
    cursor: move;

    .flip-list-move {
      transition: transform 0.5s;
    }
  }
}
</style>