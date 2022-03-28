<template>
  <div class="jm-log-viewer">
    <div class="operation">
      <jm-tooltip :content="downloading ? '下载中，请稍后...' : '下载'"
                  :placement="downloading? 'top-end': 'top'"
                  :append-to-body="false">
        <div :class="{download: true, doing: downloading}" @click="download"></div>
      </jm-tooltip>
      <div class="separator"></div>
      <jm-text-copy :async-value="() => getLog(true)"/>
    </div>
    <div class="auto-scroll" @click="handleAutoScroll(true)"
         :style="{visibility: `${autoScroll? 'hidden': 'visible'}`}"></div>
    <div class="no-bg" :style="{width: `${noWidth}px`}"></div>
    <div class="content" ref="contentRef">
      <div v-if="value && more" class="more-line">
        查看更多日志，请<span :class="{
          'download-txt': true,
          doing: downloading,
        }" @click="download">{{ downloading ? '下载中，请稍后...' : '下载' }}</span>
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
import { defineComponent, nextTick, onMounted, PropType, ref, watch } from 'vue';

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
    more: {
      type: Boolean,
      default: false,
    },
    fetchLog: Function as PropType<(isCopy: boolean) => Promise<string>>,
  },
  setup(props) {
    const data = ref<string[]>([]);
    const contentRef = ref<HTMLDivElement>();
    const noWidth = ref<number>(0);
    const autoScroll = ref<boolean>(true);
    const downloading = ref<boolean>(false);

    const virtualNoDiv = document.createElement('div');
    virtualNoDiv.style.position = 'fixed';
    virtualNoDiv.style.left = '-1000px';
    virtualNoDiv.style.top = '-1000px';
    virtualNoDiv.style.margin = '0px';
    virtualNoDiv.style.padding = '0px';
    virtualNoDiv.style.borderWidth = '0px';
    virtualNoDiv.style.height = '0px';
    virtualNoDiv.style.visibility = 'hidden';

    const handleAutoScroll = (val: boolean) => {
      autoScroll.value = val;

      if (val) {
        nextTick(() => (contentRef.value!.scrollTop = contentRef.value!.scrollHeight));
      }
    };

    const setLog = (value: string) => {
      data.value = value.split(/\r?\n/);

      virtualNoDiv.innerHTML = data.value.length + '';
      const tempNoWidth = virtualNoDiv.clientWidth + 25;
      if (tempNoWidth > noWidth.value) {
        noWidth.value = tempNoWidth;
      }

      if (autoScroll.value) {
        nextTick(() => (contentRef.value!.scrollTop = contentRef.value!.scrollHeight));
      }
    };

    onMounted(() => {
      contentRef.value?.appendChild(virtualNoDiv);
      contentRef.value?.addEventListener('scroll', () =>
        handleAutoScroll(contentRef.value!.scrollHeight - contentRef.value!.scrollTop <= contentRef.value!.clientHeight));

      nextTick(() => setLog(props.value));
    });

    watch(() => props.value, (value: string) => setLog(value));

    const getLog = async (isCopy: boolean) => {
      if (props.more && props.fetchLog) {
        return await props.fetchLog(isCopy);
      }

      return props.value;
    };

    return {
      data,
      contentRef,
      noWidth,
      autoScroll,
      downloading,
      handleAutoScroll,
      getLog,
      download: async () => {
        if (downloading.value) {
          return;
        }
        downloading.value = true;
        try {
          const log = await getLog(false);
          const blob = new Blob([log]);

          const url = window.URL.createObjectURL(blob);

          const a = document.createElement('a');
          a.href = url;
          a.download = props.filename;
          a.click();

          // 释放url
          window.URL.revokeObjectURL(url);
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