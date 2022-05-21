<template>
  <div class="jm-workflow-viewer-toolbar">
    <div class="group" v-if="!dslMode">
      <template v-if="fullscreenEnabled">
        <jm-tooltip :content="isFullscreen? '退出全屏' : '全屏'" placement="top" :appendToBody="false">
          <div :class="isFullscreen? 'screen-normal-icon' : 'screen-full-icon'"
               @click="handleFullScreen(!isFullscreen)"></div>
        </jm-tooltip>
        <div class="separator"></div>
      </template>
      <jm-tooltip v-if="zoom !== ORIGINAL_ZOOM" content="原始大小" placement="top" :appendToBody="false">
        <div class="full-icon" @click="normalize"></div>
      </jm-tooltip>
      <jm-tooltip v-else content="适屏" placement="top" :appendToBody="false">
        <div class="normal-icon" @click="fitViewer"></div>
      </jm-tooltip>
      <template v-if="isWorkflow">
        <div class="separator"></div>
        <jm-tooltip content="旋转" placement="top" :appendToBody="false">
          <div class="rotate-icon" @click="rotate"></div>
        </jm-tooltip>
      </template>
    </div>
    <div class="group" v-if="!dslMode">
      <jm-tooltip content="缩小" placement="top" :appendToBody="false">
        <div :class="{'narrow-icon': true, disabled: zoom === MIN_ZOOM}"
             @click="changeZoom(false)"></div>
      </jm-tooltip>
      <div class="percentage">{{ zoom }}%</div>
      <jm-tooltip content="放大" placement="top" :appendToBody="false">
        <div :class="{'enlarge-icon': true, disabled: zoom === MAX_ZOOM}"
             @click="changeZoom(true)"></div>
      </jm-tooltip>
    </div>
    <div class="group" v-if="!readonly && !dslMode">
      <jm-tooltip content="流程日志" placement="top" :appendToBody="false">
        <div class="process-log-icon"
             @click="processLog"></div>
      </jm-tooltip>
    </div>
    <div :class="{group: true, dsl: dslMode}">
      <jm-tooltip content="查看DSL" placement="top" :appendToBody="false" v-if="!dslMode">
        <div class="dsl-icon"
             @click="viewDsl(true)"></div>
      </jm-tooltip>
      <jm-tooltip placement="top" :appendToBody="false" v-else>
        <template #content>
          <div style="white-space: nowrap;">{{ `返回${isWorkflow ? '流程' : '管道'}` }}</div>
        </template>
        <div :class="isWorkflow ? 'workflow-icon' : 'pipeline-icon'"
             @click="viewDsl(false)"></div>
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
  emits: ['on-zoom', 'click-process-log', 'update:dsl-mode', 'on-fullscreen', 'rotate'],
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
      processLog: () => {
        emit('click-process-log');
      },
      viewDsl: (mode: boolean) => {
        emit('update:dsl-mode', mode);
      },
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

<style scoped lang="less">
.jm-workflow-viewer-toolbar {
  position: absolute;
  z-index: 1;
  top: 20px;
  right: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;

  .group + .group {
    margin-left: 20px;
  }

  .group {
    padding: 10px 15px;
    box-shadow: 0 0 4px 0 rgba(194, 194, 194, 0.5);
    border-radius: 2px;
    border: 1px solid #CAD6EE;
    background-color: rgba(255, 255, 255, 0.6);
    display: flex;
    align-items: center;

    &.dsl {
      background-color: #818894;
      border: 1px solid #767F91;
      box-shadow: 0 0 4px 0 rgba(194, 194, 194, 0.5);
    }

    > [class*='-icon'] {
      width: 24px;
      height: 24px;
      background-repeat: no-repeat;
      cursor: pointer;

      &.disabled {
        cursor: not-allowed;
      }
    }

    .process-log-icon {
      background-image: url('./svgs/tool/process-log.svg');
    }

    .dsl-icon {
      background-image: url('./svgs/tool/dsl.svg');
      background-size: 24px;
    }

    .workflow-icon {
      background-image: url('./svgs/tool/workflow.svg');
    }

    .pipeline-icon {
      background-image: url('./svgs/tool/pipeline.svg');
    }

    .screen-full-icon {
      background-image: url('./svgs/tool/screen-full.svg');

      &:hover {
        background-image: url('./svgs/tool/screen-full-hover.svg');
      }
    }

    .screen-normal-icon {
      background-image: url('./svgs/tool/screen-normal.svg');

      &:hover {
        background-image: url('./svgs/tool/screen-normal-hover.svg');
      }
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

    .rotate-icon {
      background-image: url('./svgs/tool/rotate.svg');

      &:hover {
        background-image: url('./svgs/tool/rotate-hover.svg');
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