<template>
  <jm-text-viewer
    class="jm-timer"
    :threshold="0"
    :value="time"
    @loaded="({ contentMaxWidth }) => $emit('loaded', contentMaxWidth)"
    :tip-append-to-body="tipAppendToBody"
  />
</template>

<script lang="ts">
import { defineComponent, onBeforeUnmount, onUpdated, ref } from 'vue';
import Timer from '@/components/timer/model';

export default defineComponent({
  name: 'jm-timer',
  props: {
    // 开始时间（格式：yyyy-MM-ddTHH:mm:ss）
    startTime: {
      type: String,
    },
    // 结束时间（格式：yyyy-MM-ddTHH:mm:ss）
    endTime: {
      type: String,
    },
    // toolTip是否放在body里面
    tipAppendToBody: {
      type: Boolean,
      default: true,
    },
    // 简写
    abbr: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['loaded'],
  setup(props) {
    const time = ref<string>();
    const timer = new Timer(props.startTime, props.endTime, props.tipAppendToBody, props.abbr);
    timer.getTime(t => {
      time.value = t;
    });
    onUpdated(() => {
      const timer = new Timer(props.startTime, props.endTime, props.tipAppendToBody, props.abbr);
      timer.getTime(t => {
        time.value = t;
      });
    });
    onBeforeUnmount(() => {
      timer.clearTimer();
    });
    return {
      time,
    };
  },
});
</script>
