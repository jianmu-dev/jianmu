<template>
  <jm-text-viewer class="jm-timer" :value="time" :tip-append-to-body="tipAppendToBody" :threshold="0" />
</template>

<script lang="ts">
import { computed, defineComponent, onBeforeUnmount, ref } from 'vue';

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
    const now = ref<Date>(new Date());
    const timer = setInterval(() => {
      now.value = new Date();
    }, 1000);
    const time = computed<string>(() => {
      if (!props.startTime && !props.endTime) {
        return '无';
      }
      let startTimeMillis;
      if (!props.startTime) {
        startTimeMillis = now.value.getTime();
      } else {
        startTimeMillis = Date.parse(props.startTime);
      }
      let endTimeMillis;
      if (!props.endTime) {
        endTimeMillis = now.value.getTime();
      } else {
        endTimeMillis = Date.parse(props.endTime);
      }
      const millisecond = endTimeMillis - startTimeMillis;
      if (millisecond < 0) {
        return '无';
      }
      const days = Math.floor(millisecond / (1000 * 60 * 60 * 24));
      const hours = Math.floor((millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((millisecond % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.round((millisecond % (1000 * 60)) / 1000);

      let result = '';

      if (days > 0) {
        result += `${days}d `;
      }

      if (hours > 0 && hours < 24) {
        result += `${hours}h `;
      } else if (hours === 24) {
        result += '0h ';
      }

      if (minutes > 0 && minutes < 60) {
        result += `${minutes}m `;
      } else if (minutes === 60) {
        result += '0m ';
      }
      if (seconds >= 0 && seconds < 1) {
        result += '不足1s';
      } else if (seconds >= 1 && seconds < 60) {
        result += `${seconds}s`;
      } else if (seconds === 60) {
        result += '0s';
      }
      if (props.abbr) {
        const arr = result.split(' ');
        if (arr.length > 2) {
          return `${arr[0]} ${arr[1]}`;
        }
      }
      return result || '无';
    });
    onBeforeUnmount(() => {
      clearInterval(timer);
    });
    return {
      time,
    };
  },
});
</script>
