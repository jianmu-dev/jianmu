<template>
  <div class="jm-state">
    <div class="signal" :style="signalColor? { backgroundColor: signalColor } : null"></div>
    <div class="label">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, computed } from 'vue';

const DEFAULT_SIGNAL_COLOR = '#096DD9';
type StateType = '' | 'success' | 'warning' | 'info' | 'error';

export default defineComponent({
  name: 'jm-state',
  props: {
    type: String as PropType<StateType>,
    customSignalColor: String,
  },
  setup(props) {
    const signalColor = computed(() => {
      const { customSignalColor } = props;

      let sColor;

      if (customSignalColor) {
        sColor = customSignalColor;
      } else {
        const type = props.type || 'info';

        switch (type) {
          case 'success':
            sColor = '#51C41B';
            break;
          case 'warning':
            sColor = '#979797';
            break;
          case 'info':
            sColor = DEFAULT_SIGNAL_COLOR;
            break;
          case 'error':
            sColor = '#FF4D4F';
            break;
        }
      }

      return sColor;
    });

    return {
      signalColor,
    };
  },
});
</script>

<style scoped lang="less">
.jm-state {
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  color: #666666;

  .signal {
    width: 6px;
    height: 6px;
    margin-right: 5px;
    border-radius: 100%;
    overflow: hidden;
  }

  .label {

  }
}
</style>