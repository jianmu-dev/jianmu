<template>
  <transition-group type="transition" name="list" tag="div" class="jm-sorter">
    <slot></slot>
  </transition-group>
</template>
<script lang="ts">
import { defineComponent, onMounted, onUpdated, PropType, ref } from 'vue';
import Sorter from './model';

export default defineComponent({
  name: 'jm-sorter',
  props: {
    modelValue: {
      type: Array as PropType<object[]>,
      required: true,
    },
  },
  emits: ['change', 'update:model-value'],
  setup(props, { emit }) {
    const data = ref(props.modelValue);
    let sorter: Sorter;
    const completeCallback = () => {
      const arr = [...props.modelValue];
      const { oldIndex, newIndex, oldElement, newElement, originArr } = sorter!.complete(arr);
      emit('update:model-value', arr);
      emit('change', {
        oldIndex,
        newIndex,
        oldElement,
        newElement,
        originArr,
      });
    };
    onMounted(() => {
      sorter = new Sorter(completeCallback);
    });
    onUpdated(() => {
      if (data.value === props.modelValue) {
        return;
      }
      data.value = props.modelValue;
      sorter = new Sorter(completeCallback);
    });
  },
});
</script>
<style lang="less">
.jm-sorter {
  display: flex;
  flex-wrap: wrap;

  & > [draggable=true] {
    cursor: move;
    position: relative;
    transition: all 0.2s linear;
  }

  .drag-target-insertion {
    position: absolute;
    z-index: 9999;
    width: 8px;
    box-sizing: border-box;
    border: 2px solid #096DD9;
    background: rgba(9, 109, 217, 0.3);
  }
}
</style>
