<template>
  <div class="folding">
    <div class="prefix">
      <slot name="prefix" />
      <div class="title">
        <slot name="title" />
      </div>
    </div>
    <transition name="folding">
      <slot v-if="status || pageAble"> </slot>
    </transition>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  props: {
    status: {
      type: Boolean,
      required: true,
    },
    pageAble: {
      type: Boolean,
      default: true,
    },
  },
});
</script>

<style scoped lang="less">
@keyframes appearIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

.folding {
  .prefix {
    display: flex;
    align-items: center;
    margin-bottom: 10px;

    .title {
      flex: 1;
    }
  }

  .folding-enter-active {
    animation: appearIn 0.3s;
  }

  .folding-leave-active {
    animation: appearIn reverse 0.1s;
  }
}
</style>
