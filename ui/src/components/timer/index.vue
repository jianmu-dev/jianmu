<template>
  <jm-text-viewer class="jm-timer" :value="time" :tip-append-to-body="tipAppendToBody" :threshold="0" />
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
