<template>
  <div class="jm-log-viewer" ref="logViewerRef">
    <div class="operation">
      <jm-tooltip
        :content="downloading ? '下载中，请稍后...' : '下载'"
        :placement="downloading ? 'top-end' : 'top'"
        :append-to-body="false"
      >
        <div :class="{ download: true, doing: downloading }" @click="downloadLog"></div>
      </jm-tooltip>
      <div class="separator"></div>
      <jm-text-copy :async-value="copy" />
    </div>
    <div
      class="auto-scroll"
      @click="handleAutoScroll(true)"
      :style="{ visibility: `${autoScroll ? 'hidden' : 'visible'}` }"
    ></div>
    <div class="no-bg" :style="{ width: `${noWidth}px` }"></div>
    <div class="content">
      <div v-if="moreLog" class="more-line">日志过大，更多日志请下载查看</div>
      <div class="line" v-for="(txt, idx) in data" :key="idx" :style="{ marginLeft: `${noWidth}px` }">
        <div class="no" :style="{ left: `${-1 * noWidth}px`, width: `${noWidth}px` }">
          {{ idx + 1 }}
        </div>
        <pre class="txt">{{ txt }}</pre>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, nextTick, onBeforeUnmount, onMounted, onUpdated, PropType, ref } from 'vue';
import LogViewer, { CallBackFnType, DownloadFnType } from './model';

export default defineComponent({
  name: 'jm-log-viewer',
  props: {
    filename: {
      type: String,
      default: 'log.txt',
    },
    value: {
      type: String,
      default: '',
    },
    url: {
      type: String,
      default: '',
    },
    download: Function as PropType<DownloadFnType>,
  },
  setup(props) {
    const data = ref<string[]>([]);
    const logViewerRef = ref<HTMLDivElement>();
    const contentRef = computed<HTMLDivElement | undefined>(() => {
      const el = logViewerRef.value?.lastElementChild;
      if (!el) {
        return undefined;
      }
      return el as HTMLDivElement;
    });
    const noWidth = ref<number>(0);
    const autoScroll = ref<boolean>(true);
    const downloading = ref<boolean>(false);
    const moreLog = ref<boolean>(false);

    let logViewer: LogViewer;

    const handleAutoScroll = (val: boolean) => {
      autoScroll.value = val;
    };

    const callback: CallBackFnType = async (logData, startLine) => {
      data.value.push(...logData);
      moreLog.value = startLine === undefined ? false : startLine > 1;

      await nextTick();
      const tempNoWidth = logViewer.calculateTempNoWidth(data.value.length);
      noWidth.value = tempNoWidth > noWidth.value ? tempNoWidth : noWidth.value;
    };

    const initData = () => {
      data.value = [];
    };

    onMounted(() => {
      logViewer = new LogViewer(
        logViewerRef.value!,
        props.filename,
        props.value,
        props.url,
        props.download,
        callback,
        () => autoScroll.value,
        handleAutoScroll,
      );
      logViewer.listen(data.value);
    });

    onUpdated(() => {
      if (logViewer.checkValue(props.url, props.value)) {
        return;
      }
      logViewer.destroy();
      initData();
      logViewer = new LogViewer(
        logViewerRef.value!,
        props.filename,
        props.value,
        props.url,
        props.download,
        callback,
        () => autoScroll.value,
        handleAutoScroll,
      );
      logViewer.listen(data.value);
    });

    onBeforeUnmount(() => {
      logViewer.destroy();
      initData();
    });

    return {
      data,
      contentRef,
      logViewerRef,
      noWidth,
      autoScroll,
      downloading,
      handleAutoScroll,
      copy: () => {
        return logViewer.copy(data.value, moreLog.value);
      },
      moreLog,
      downloadLog: async () => {
        if (downloading.value) {
          return;
        }
        downloading.value = true;
        try {
          await logViewer.downLoad();
        } catch (err) {
          console.warn(err.message);
        } finally {
          downloading.value = false;
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-log-viewer {
  position: relative;
  background-color: #19253b;
  box-sizing: border-box;
  height: 100%;

  .no-bg {
    position: absolute;
    left: 0;
    top: 0;
    width: 0;
    height: 100%;
    background-color: #303d56;
  }

  .content {
    width: 100%;
    height: 100%;
    overflow: auto;
    line-height: 22px;

    .more-line {
      text-align: center;
      color: #ffffff;
      padding-bottom: 10px;
    }

    .line {
      position: relative;
      min-height: 22px;

      .no {
        // 解决数字字体等宽问题
        font-family: 'Helvetica Neue';
        position: absolute;
        top: 0;
        user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        -webkit-user-select: none;
        text-align: right;
        padding-right: 10px;
        box-sizing: border-box;
        color: #8193b2;
      }

      .txt {
        margin: 0 16px;
        word-wrap: break-word;
        white-space: pre-wrap;
        word-break: normal;
        color: #ffffff;
      }
    }
  }

  &:hover {
    .operation {
      visibility: visible;
    }
  }

  .operation {
    position: absolute;
    right: 10px;
    top: 15px;
    z-index: 1;

    display: flex;
    align-items: center;
    padding: 5px 10px;
    background-color: #818894;
    box-shadow: 0 0 4px 0 rgba(194, 194, 194, 0.5);
    border-radius: 2px;
    border: 1px solid #767f91;
    white-space: nowrap;

    visibility: hidden;

    .download {
      width: 20px;
      height: 20px;
      padding: 4px;
      background-image: url('./svgs/download.svg');
      background-repeat: no-repeat;
      background-size: 20px;
      background-position: center;
      cursor: pointer;

      &.doing {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    ::v-deep(.jm-text-copy) {
      font-size: 20px;

      .jm-icon-button-copy {
        color: #ffffff;
        opacity: 1;
      }
    }

    .separator {
      margin: 0 10px;
      width: 1px;
      height: 15px;
      background-color: #cdd1e3;
      overflow: hidden;
    }
  }

  .auto-scroll {
    position: absolute;
    right: 25px;
    bottom: 25px;
    z-index: 1;

    visibility: hidden;

    width: 24px;
    height: 24px;
    border-radius: 3px;
    background-color: #19253b;
    background-image: url('./svgs/auto-scroll.svg');
    background-repeat: no-repeat;
    background-size: contain;
    cursor: pointer;
  }
}
</style>
