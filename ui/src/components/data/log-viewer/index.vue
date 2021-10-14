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
    <textarea ref="textareaRef"></textarea>
  </div>
</template>

<script type="text/ecmascript-6">
import { defineComponent, getCurrentInstance, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import useClipboard from 'vue-clipboard3';
// 引入全局实例
import CodeMirror from 'codemirror';

// 核心样式
import 'codemirror/lib/codemirror.css';
// 引入主题后，还需要在 options 中指定主题才会生效
import 'codemirror/theme/ayu-mirage.css';

// 需要引入具体的语法高亮库才会有对应的语法高亮效果
// codemirror 官方其实支持通过 /addon/mode/loadmode.js 和 /mode/meta.js 来实现动态加载对应语法高亮库
// 但 vue 貌似没有无法在实例初始化后再动态加载对应 JS ，所以此处才把对应的 JS 提前引入
import './mode';

// 尝试获取全局实例
const codemirror = window.CodeMirror || CodeMirror;

/**
 * 初始化
 * @param textarea
 * @returns CodeMirror
 */
function initialize(textarea) {
  return codemirror.fromTextArea(textarea, {
    // 模式
    mode: 'log',
    // 缩进格式
    tabSize: 2,
    // 主题，对应主题库 JS 需要提前引入
    theme: 'ayu-mirage',
    // 强制换行
    lineWrapping: true,
    // 显示行号
    lineNumbers: true,
    // 是否只读
    readOnly: true,
  });
}

export default defineComponent({
  name: 'jm-log-viewer',
  props: {
    filename: String,
    value: String,
    autoScroll: {
      type: Boolean,
      default: false,
    },
  },
  setup(props) {
    const { proxy } = getCurrentInstance();
    const { toClipboard } = useClipboard();
    const textareaRef = ref(null);
    const previousHeight = ref(0);
    const autoScrollInterval = ref();

    let instance;

    const scrollToEnd = autoScroll => {
      if (!autoScroll) {
        return;
      }

      // 获取滚动信息
      const sc = instance.getScrollInfo();

      if (sc.height > previousHeight.value) {
        // 滚动条定位到末尾
        instance.scrollTo(sc.left, sc.height);

        previousHeight.value = sc.height;
      }
    };

    onMounted(() => {
      // 设置textarea值
      textareaRef.value.value = props.value;

      // 初始化
      instance = initialize(textareaRef.value);

      watch(() => props.value, value => {
        // Remove the editor, and restore the original textarea
        instance.toTextArea();

        // 更新textarea值
        textareaRef.value.value = value;

        // 重新初始化
        instance = initialize(textareaRef.value);
      });

      watch(() => props.autoScroll, autoScroll => {
        // 立即执行一次
        scrollToEnd(autoScroll);

        clearInterval(autoScrollInterval.value);

        if (autoScroll) {
          autoScrollInterval.value = setInterval(() => scrollToEnd(autoScroll), 500);
        }
      });
    });

    onBeforeUnmount(() => clearInterval(autoScrollInterval.value));

    return {
      textareaRef,
      download: () => {
        const blob = new Blob([props.value]);

        const url = window.URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = props.filename || 'log.txt';
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
  z-index: 0;
  // 必须与codemirror主题背景保持一致
  background-color: #1f2430;

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

  ::v-deep(.CodeMirror) {
    z-index: 0;
    height: inherit;

    .CodeMirror-gutters {
      z-index: 0;
    }

    .CodeMirror-linenumber {
      // 字体设为等宽
      font-family: 'Helvetica Neue';
      color: #C1D7FF;
      font-weight: 400;
      line-height: 20px;
    }
  }
}
</style>