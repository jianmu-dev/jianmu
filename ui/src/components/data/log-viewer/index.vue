<template>
  <div class="jm-log-viewer">
    <div class="operation">
      <jm-tooltip content="下载" placement="top">
        <div class="download" @click="download"></div>
      </jm-tooltip>
      <div class="separator"></div>
      <jm-tooltip content="复制" placement="top">
        <div class="copy" @click="copy"></div>
      </jm-tooltip>
    </div>
    <div class="auto-scroll" @click="handleAutoScroll(true)"
         :style="{visibility: `${autoScroll? 'hidden': 'visible'}`}"></div>
    <div class="no-bg" :style="{width: `${noWidth}px`}"></div>
    <div class="content" ref="contentRef">
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
import { defineComponent, getCurrentInstance, nextTick, onMounted, ref, watch } from 'vue';
import useClipboard from 'vue-clipboard3';

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
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const { toClipboard } = useClipboard();
    const data = ref<string[]>([]);
    const contentRef = ref<HTMLDivElement>();
    const noWidth = ref<number>(0);
    const autoScroll = ref<boolean>(true);

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

      setLog(props.value);
    });

    watch(() => props.value, (value: string) => setLog(value));

    return {
      data,
      contentRef,
      noWidth,
      autoScroll,
      handleAutoScroll,
      download: () => {
        const blob = new Blob([props.value]);

        const url = window.URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = props.filename;
        a.click();

        // 释放url
        window.URL.revokeObjectURL(url);
      },
      copy: async () => {
        try {
          await toClipboard(props.value);
          proxy.$success('复制成功');
        } catch (err) {
          proxy.$error('复制失败，请手动复制');
          console.error(err);
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

    .line {
      position: relative;

      .no {
        // 解决数字字体等宽问题
        font-family: 'Helvetica Neue';
        position: absolute;
        right: 0;
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

    visibility: hidden;

    .download {
      width: 20px;
      height: 20px;
      background-image: url('./svgs/download.svg');
      background-repeat: no-repeat;
      background-size: contain;
      cursor: pointer;
    }

    .copy {
      width: 20px;
      height: 20px;
      background-image: url('./svgs/copy.svg');
      background-repeat: no-repeat;
      background-size: contain;
      cursor: pointer;
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