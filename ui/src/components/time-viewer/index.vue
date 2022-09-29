<template>
  <span>{{ timeText }}</span>
</template>

<script lang="ts">
import { defineComponent, onBeforeUnmount, onMounted, ref } from 'vue';

export default defineComponent({
  name: 'jm-time-viewer',
  props: {
    // 结束时间（格式：yyyy-MM-ddTHH:mm:ss）
    value: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    let timer: number;
    const timeText = ref<string>('');
    const timeToEnd = (endTime: number): string => {
      // 过了多少秒 (当前时间戳 减去 结束时间戳)
      const nowTime = new Date().getTime();
      const second = Math.floor((nowTime - endTime) / 1000);
      if (second <= 60) {
        // 一分钟内
        return '刚刚';
      } else if (second > 60 && second <= 3600) {
        // 几分钟前 一个小时内(60 * 60) 60个60秒 = 3600
        const minute = Math.floor(second / 60);
        return minute + '分钟前';
      } else if (second > 3600 && second <= 86400) {
        // 几小时前 一天之内 (24 * 3600) 24个3600秒 = 86400
        const hour = Math.floor(second / 3600);
        return hour + '小时前';
      } else if (second > 86400 && second <= 2592000) {
        // 一个月之内 (30 * 86400) 30个86400秒 = 2592000
        const day = Math.floor(second / 86400);
        return day + '天前';
      } else if (second > 2592000 && second <= 31104000) {
        // 一年之内 (12 * 2592000) 12个2592000秒 = 31104000
        const month = Math.floor(second / 2592000);
        return month + '月前';
      } else {
        // 一年之上
        const year = Math.floor(second / 2592000);
        return year + '年前';
      }
    };
    onMounted(() => {
      const setTimer = (starTime: number, interval = 1000, callback: (n: number) => string) => {
        timer = setInterval(() => {
          timeText.value = callback(starTime);
        }, interval);
      };
      const second = Math.floor((new Date().getTime() - new Date(props.value).getTime()) / 1000);
      // 没超过一天设置定时器
      if (second <= 86400) {
        // 超过一分钟 定时器一分钟执行一次; 超过一小时 定时器一小时执行一次
        const interval = second >= 3600 ? 3600000 : second >= 60 ? 60000 : 1000;
        timeText.value = timeToEnd(new Date(props.value).getTime());
        setTimer(new Date(props.value).getTime(), interval, timeToEnd);
      } else {
        timeText.value = timeToEnd(new Date(props.value).getTime());
      }
    });
    onBeforeUnmount(() => {
      if (timer) {
        clearInterval(timer);
      }
    });
    return {
      timeText,
    };
  },
});
</script>
