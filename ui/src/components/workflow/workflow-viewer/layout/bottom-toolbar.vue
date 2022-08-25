<template>
  <div class="jm-workflow-viewer-toolbar">
    <div class="group leftbar" v-if="!dslMode">
      <template v-if="fullscreenEnabled">
        <jm-tooltip :content="isFullscreen? '退出全屏' : '全屏'" placement="top" :appendToBody="false">
          <div :class="isFullscreen? 'jm-icon-workflow-screen-normal' : 'jm-icon-workflow-screen-full'"
               @click="handleFullScreen(!isFullscreen)"></div>
        </jm-tooltip>
        <div class="separator"></div>
      </template>
      <jm-tooltip v-if="zoom !== ORIGINAL_ZOOM" content="原始大小" placement="top" :appendToBody="false">
        <div class="jm-icon-workflow-full" @click="normalize"></div>
      </jm-tooltip>
      <jm-tooltip v-else content="适屏" placement="top" :appendToBody="false">
        <div class="jm-icon-workflow-normal" @click="fitViewer"></div>
      </jm-tooltip>
      <template v-if="isWorkflow && !isX6">
        <div class="separator"></div>
        <jm-tooltip content="旋转" placement="top" :appendToBody="false">
          <div class="jm-icon-workflow-rotate" @click="rotate"></div>
        </jm-tooltip>
      </template>
      <div class="separator"></div>
    </div>
    <div class="group rightbar" v-if="!dslMode">
      <jm-tooltip content="缩小" placement="top" :appendToBody="false">
        <div :class="{'jm-icon-workflow-zoom-out':true, disabled: zoom === MIN_ZOOM}"
             @click="changeZoom(false)"></div>
      </jm-tooltip>
      <div class="percentage">{{ zoom }}%</div>
      <jm-tooltip content="放大" placement="top" :appendToBody="false">
        <div :class="{'jm-icon-workflow-zoom-in': true, disabled: zoom === MAX_ZOOM}"
             @click="changeZoom(true)"></div>
      </jm-tooltip>
    </div>
</div>
</template>

<script lang="ts">
import { computed, defineComponent, onBeforeUpdate, PropType, ref, SetupContext } from 'vue';
import { DslTypeEnum } from '@/api/dto/enumeration';
import screenfull from 'screenfull';

const ZOOM_INTERVAL = 10;
const ORIGINAL_ZOOM = 100;
const MIN_ZOOM = 20;
const MAX_ZOOM = 500;

export default defineComponent({
  props: {
    readonly: {
      type: Boolean,
      default: false,
    },
    dslType: {
      type: String as PropType<DslTypeEnum>,
      required: true,
    },
    isX6: {
      type: Boolean,
      default: false,
    },
    dslMode: {
      type: Boolean,
      required: true,
    },
    zoomValue: {
      type: Number,
      default: MAX_ZOOM,
    },
    fullscreenEl: HTMLElement,
  },
  emits: ['on-zoom', 'on-fullscreen', 'rotate'],
  setup(props, { emit }: SetupContext) {
    const zoom = ref<number>(props.zoomValue);
    const fullscreenEnabled = ref<boolean>(screenfull.isEnabled);
    const isFullscreen = ref<boolean>(screenfull.isFullscreen);

    if (screenfull.isEnabled) {
      screenfull.on('change', () => {
        isFullscreen.value = screenfull.isFullscreen;
        emit('on-fullscreen', isFullscreen.value);
      });
    }

    onBeforeUpdate(() => {
      zoom.value = props.zoomValue;
    });

    return {
      zoom,
      fullscreenEnabled,
      isFullscreen,
      ORIGINAL_ZOOM,
      MIN_ZOOM,
      MAX_ZOOM,
      isWorkflow: computed<boolean>(() => props.dslType === DslTypeEnum.WORKFLOW),
      handleFullScreen: (val: boolean) => {
        if (val) {
          screenfull.request(props.fullscreenEl);
        } else {
          screenfull.exit();
        }
      },
      normalize: () => {
        emit('on-zoom', ORIGINAL_ZOOM);
      },
      fitViewer: () => {
        emit('on-zoom');
      },
      rotate: () => {
        emit('rotate');
      },
      changeZoom: (enlarging: boolean) => {
        let temp = zoom.value + (enlarging ? 1 : -1) * ZOOM_INTERVAL;
        const remainder = temp % ZOOM_INTERVAL;
        temp -= remainder;
        if (!enlarging && remainder > 0) {
          temp += ZOOM_INTERVAL;
        }

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

<style lang="less" scoped>
.jm-workflow-viewer-toolbar {
  position: absolute;
  z-index: 1;
  bottom: 20px;
  right: 30px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background-color: #FFFFFF;
  box-shadow: 0px 0px 4px 0px rgb(194 194 194 / 50%);

  .group {
    opacity: 1;
    padding: 8px 0px;
    display: flex;
    align-items: center;

    .separator {
      margin: 0 16px;
      width: 1px;
      height: 15px;
      background-color: #CDD1E3;
      overflow: hidden;
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
  .leftbar {
    padding-left: 20px;
    div[class*='jm-icon-workflow-'] {
      &:before {
        color: #000;
        font-size: 18px;
        font-weight: 500;
      }
      &:hover {
        // background-color: #EFF7FF;
        cursor: pointer;
        color: #096DD9;
      }
    }
  }
  .rightbar {
    padding-right: 20px;
    .jm-icon-workflow-zoom-in {
      font-size: 24px;
      line-height: 24px;
      color: #748394;
      &::before {
        margin: 0;
      }
      &:hover {
        cursor: pointer;
        color: #096DD9;
      }
      &.disabled {
        cursor: not-allowed;
      }
    }
    .jm-icon-workflow-zoom-out {
      font-size: 24px;
      line-height: 24px;
      color: #748394;
      &:hover {
        cursor: pointer;
        color: #096DD9;
      }
      &::before {
        margin: 0;
      }
      &.disabled {
        cursor: not-allowed;
      }
    }
  }
}
</style>