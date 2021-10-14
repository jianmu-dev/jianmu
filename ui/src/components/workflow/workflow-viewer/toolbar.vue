<template>
  <div class="jm-workflow-viewer-toolbar">
    <div class="group">
      <jm-tooltip content="原始大小" placement="top">
        <div class="full-icon" @click="normalize"></div>
      </jm-tooltip>
      <div class="separator"></div>
      <jm-tooltip content="适屏" placement="top">
        <div class="normal-icon" @click="fitViewer"></div>
      </jm-tooltip>
    </div>
    <div class="group">
      <jm-tooltip content="缩小" placement="top">
        <div :class="{'narrow-icon': true, disabled: zoom === MIN_ZOOM}"
             @click="changeZoom(false)"></div>
      </jm-tooltip>
      <div class="percentage">{{ zoom }}%</div>
      <jm-tooltip content="放大" placement="top">
        <div :class="{'enlarge-icon': true, disabled: zoom === MAX_ZOOM}"
             @click="changeZoom(true)"></div>
      </jm-tooltip>
    </div>
    <div class="group">
      <jm-tooltip content="流程日志" placement="top">
        <div class="process-log-icon" @click="processLog"></div>
      </jm-tooltip>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onBeforeUpdate, ref, SetupContext } from 'vue';

const ZOOM_INTERVAL = 10;
const ORIGINAL_ZOOM = 100;
const MIN_ZOOM = 20;
const MAX_ZOOM = 500;

export default defineComponent({
  props: {
    'zoomValue': {
      type: Number,
      default: MAX_ZOOM,
    },
  },
  emits: ['on-zoom', 'click-process-log'],
  setup(props, { emit }: SetupContext) {
    const zoom = ref<number>(props.zoomValue);

    onBeforeUpdate(() => {
      zoom.value = props.zoomValue;
    });

    return {
      zoom,
      MIN_ZOOM,
      MAX_ZOOM,
      processLog: () => {
        emit('click-process-log');
      },
      normalize: () => {
        emit('on-zoom', ORIGINAL_ZOOM);
      },
      fitViewer: () => {
        emit('on-zoom');
      },
      changeZoom: (enlarging: boolean) => {
        let temp = zoom.value + (enlarging ? 1 : -1) * ZOOM_INTERVAL;
        temp = temp - temp % ZOOM_INTERVAL;

        if (temp < MIN_ZOOM) {
          temp = MIN_ZOOM;
        } else if (temp > MAX_ZOOM) {
          temp = MAX_ZOOM;
        }

        emit('on-zoom', temp);
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-viewer-toolbar {
  position: absolute;
  z-index: 1;
  top: 22px;
  right: 44px;
  display: flex;
  justify-content: flex-end;
  align-items: center;

  .group + .group{
    margin-left: 44px;
  }

  .group {
    padding: 8px 16px;
    box-shadow: 0 0 4px 0 rgba(194, 194, 194, 0.5);
    border-radius: 2px;
    border: 1px solid #CAD6EE;
    background-color: rgba(255, 255, 255, 0.6);
    display: flex;
    align-items: center;

    > [class*='-icon'] {
      width: 24px;
      height: 24px;
      background-repeat: no-repeat;
      cursor: pointer;

      &.disabled {
        cursor: not-allowed;
      }
    }

    .process-log-icon{
      background-image: url('./svgs/tool/process-log.svg');
    }

    .full-icon {
      background-image: url('./svgs/tool/full.svg');

      &:hover {
        background-image: url('./svgs/tool/full-hover.svg');
      }
    }

    .normal-icon {
      background-image: url('./svgs/tool/normal.svg');

      &:hover {
        background-image: url('./svgs/tool/normal-hover.svg');
      }
    }

    .separator {
      margin: 0 16px;
      width: 1px;
      height: 15px;
      background-color: #CDD1E3;
      overflow: hidden;
    }

    .enlarge-icon {
      background-image: url('./svgs/tool/enlarge.svg');

      &.disabled {
        background-image: url('./svgs/tool/enlarge-disabled.svg');
      }
    }

    .narrow-icon {
      background-image: url('./svgs/tool/narrow.svg');

      &.disabled {
        background-image: url('./svgs/tool/narrow-disabled.svg');
      }
    }

    .percentage {
      margin: 0 10px;
      width: 40px;
      line-height: 20px;
      font-size: 14px;
      font-weight: 500;
      color: #4A4A4A;
      text-align: center;
    }
  }
}
</style>