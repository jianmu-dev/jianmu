<template>
  <span>{{ timeText }}</span>
</template>

<script lang="ts">
import { defineComponent, onBeforeUnmount, onMounted, onUpdated, ref } from 'vue';
import TimeViewer from '@/components/time-viewer/model';

export default defineComponent({
  name: 'jm-time-viewer',
  props: {
    value: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    const timeText = ref<string>('');
    const timeValue = ref<string>(props.value);
    let timeViewer: TimeViewer;
    onUpdated(() => {
      if (props.value !== timeValue.value) {
        timeViewer.clearTimer();
        timeViewer = new TimeViewer(props.value);
        timeViewer.getTime((t: string) => {
          timeText.value = t;
        });
        timeValue.value = props.value;
      }
    });
    onMounted(() => {
      timeViewer = new TimeViewer(props.value);
      timeViewer.getTime((t: string) => {
        timeText.value = t;
      });
    });
    onBeforeUnmount(() => {
      timeViewer.clearTimer();
    });
    return {
      timeText,
    };
  },
});
</script>
