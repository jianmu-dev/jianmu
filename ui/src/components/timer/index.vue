<template>
  <jm-text-viewer class="jm-timer" :value="timeValue" :tip-append-to-body="tipAppendToBody"/>
</template>

<script lang='ts'>
import { computed, defineComponent, ref, onUpdated, onBeforeUnmount } from 'vue';

export default defineComponent({
  name: 'jm-timer',
  props: {
    // 开始时间（格式：yyyy-MM-ddTHH:mm:ss）
    startTime: {
      type: String,
      required: true,
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
  },
  setup(props) {
    /**
     * 执行时间格式器
     * @param startTime     格式：yyyy-MM-ddTHH:mm:ss
     * @param endTime       格式：yyyy-MM-ddTHH:mm:ss
     */
    const executionTimeFormatter = (startTime: string, endTime?: string): string => {
      const startTimeMillis = Date.parse(startTime);
      let endTimeMillis;
      if (!endTime) {
        endTimeMillis = new Date().getTime();
      } else {
        endTimeMillis = Date.parse(endTime);
      }
      const millisecond = endTimeMillis - startTimeMillis;
      const days = Math.floor(millisecond / (1000 * 60 * 60 * 24));
      const hours = Math.floor((millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((millisecond % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.round((millisecond % (1000 * 60)) / 1000);

      let result = '';
      if (days > 0) {
        result += `${days}d `;
      }
      if (hours > 0) {
        result += `${hours}h `;
      }
      if (minutes > 0) {
        result += `${minutes}m `;
      }
      if (seconds >= 0) {
        result += `${seconds}s`;
      }
      return result || '无';
    };

    // formatter转化后的值
    const time = ref<string>(executionTimeFormatter(props.startTime, props.endTime));
    // 最终展示的值
    const timeValue = computed<string>({
      get() {
        return time.value;
      },
      set(value) {
        time.value = value;
      },
    });
    let timer = setInterval(() => {
      timeValue.value = executionTimeFormatter(props.startTime, props.endTime);
    }, 1000);
    onUpdated(() => {
      // 如果执行完成，停止计时
      if (props.endTime && timer) {
        // 清除定时器
        clearInterval(timer);
      } else {
        timer = setInterval(() => {
          timeValue.value = executionTimeFormatter(props.startTime, props.endTime);
        }, 1000);
      }
    });
    onBeforeUnmount(() => {
      // 清除定时器
      if (!timer) {
        return;
      }
      clearInterval(timer);
    });
    return {
      timeValue,
    };
  },
});
</script>
