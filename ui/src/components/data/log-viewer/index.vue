<template>
  <div class="jm-log-viewer" ref="logViewerRef">
    <div class="operation">
      <jm-tooltip :content="downloading ? '下载中，请稍后...' : '下载'"
                  :placement="downloading? 'top-end': 'top'"
                  :append-to-body="false">
        <div :class="{download: true, doing: downloading}" @click="downloadLog"></div>
      </jm-tooltip>
      <div class="separator"></div>
      <jm-text-copy :async-value="copy"/>
    </div>
    <div class="auto-scroll" @click="handleAutoScroll(true)"
         :style="{visibility: `${autoScroll? 'hidden': 'visible'}`}"></div>
    <div class="no-bg" :style="{width: `${noWidth}px`}"></div>
    <div class="content">
      <div v-if="moreLog" class="more-line">
        <i class="jm-icon-button-loading" v-if="loadLoading"></i>
        <span :class="{
          'download-txt': true,
          doing: loadLoading,
        }" @click="loadMoreLog">{{ loadLoading ? '加载中，请稍后...' : '加载更多日志' }}</span>
      </div>
      <div class="line" v-for="(txt, idx) in data" :key="idx"
           :style="{marginLeft: `${noWidth}px`}">
        <div class="no" :style="{left: `${-1 * noWidth}px`, width: `${noWidth}px`}">
          {{ idx + 1 }}
        </div>
        <pre class="txt">{{ txt }}</pre>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, nextTick, onBeforeUnmount, onMounted, onUpdated, PropType, ref } from 'vue';
import LogViewer, { CallBackFnType, DownloadFnType, LoadMoreFnType } from './model';
import { ILogVo } from '@/api/dto/workflow-execution-record';

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
    loadMore: Function as PropType<LoadMoreFnType>,
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
    const loadLoading = ref<boolean>(false);
    const moreLog = ref<boolean>(false);

    let logViewer: LogViewer;

    const handleAutoScroll = (val: boolean) => {
      autoScroll.value = val;
    };

    const callback: CallBackFnType = async (logData, startLine) => {
      data.value = logData;
      moreLog.value = startLine === undefined ? false : startLine > 1;

      await nextTick();
      const tempNoWidth = logViewer.calculateTempNoWidth(data.value.length);
      noWidth.value = tempNoWidth > noWidth.value ? tempNoWidth : noWidth.value;
    };

    onMounted(() => {
      logViewer = new LogViewer(logViewerRef.value!, props.filename, props.value, props.url, props.download, props.loadMore,
        callback, () => autoScroll.value, handleAutoScroll);
      logViewer.listen(data.value);
    });


    onUpdated(() => {
      if (logViewer.checkValue(props.url, props.value)) {
        return;
      }
      logViewer.destroy((val: string[]) => {
        data.value = val;
      });
      logViewer = new LogViewer(logViewerRef.value!, props.filename, props.value, props.url, props.download, props.loadMore,
        callback, () => autoScroll.value, handleAutoScroll);
      logViewer.listen(data.value);
    });

    onBeforeUnmount(() => logViewer.destroy((val: string[]) => {
      data.value = val;
    }));

    return {
      data,
      contentRef,
      logViewerRef,
      noWidth,
      autoScroll,
      downloading,
      loadLoading,
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
      loadMoreLog: async () => {
        handleAutoScroll(false);

        if (loadLoading.value) {
          return;
        }
        loadLoading.value = true;
        try {
          const res = await logViewer.loadMore() as ILogVo[];
          res.reverse().forEach((item: ILogVo) => {
            data.value = [item.data, ...data.value];
          });
          moreLog.value = logViewer.isMoreLog();
        } catch (err) {
          console.warn(err.message);
        } finally {
          loadLoading.value = false;
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-log-viewer {
  position: relative;
  background-color: #19253B;
  padding: 8px 0;
  box-sizing: border-box;
  height: 100%;

  .no-bg {
    position: absolute;
    left: 0;
    top: 0;
    width: 0;
    height: 100%;
    background-color: #303D56;
  }

  .content {
    width: 100%;
    height: 100%;
    overflow: auto;
    line-height: 22px;

    .more-line {
      text-align: center;
      color: #FFFFFF;
      padding-bottom: 10px;

      .download-txt {
        color: #8193B2;
        cursor: pointer;

        &.doing {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }
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
        color: #8193B2;
      }

      .txt {
        margin: 0 16px;
        word-wrap: break-word;
        white-space: pre-wrap;
        word-break: normal;
        color: #FFFFFF;
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
    border: 1px solid #767F91;
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
        color: #FFFFFF;
        opacity: 1;
      }
    }

    .separator {
      margin: 0 10px;
      width: 1px;
      height: 15px;
      background-color: #CDD1E3;
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
    background-color: #19253B;
    background-image: url('./svgs/auto-scroll.svg');
    background-repeat: no-repeat;
    background-size: contain;
    cursor: pointer;
  }
}
</style>