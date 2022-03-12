<template>
  <div class="jm-dsl-editor" @click="clickEmpty">
    <div v-if="readonly" class="operation">
      <jm-tooltip content="复制" placement="top">
        <div class="copy" @click="copy"></div>
      </jm-tooltip>
    </div>
    <textarea ref="textareaRef"></textarea>
  </div>
</template>

<script type="text/ecmascript-6">
import { defineComponent, getCurrentInstance, onBeforeUpdate, onMounted, ref } from 'vue';
import useClipboard from 'vue-clipboard3';
// 引入全局实例
import CodeMirror from 'codemirror';
import { Comment, Tab } from './model/shortcut';

// 核心样式
import 'codemirror/lib/codemirror.css';
// 引入主题后，还需要在 options 中指定主题才会生效
import 'codemirror/theme/ayu-mirage.css';

// 需要引入具体的语法高亮库才会有对应的语法高亮效果
// codemirror 官方其实支持通过 /addon/mode/loadmode.js 和 /mode/meta.js 来实现动态加载对应语法高亮库
// 但 vue 貌似没有无法在实例初始化后再动态加载对应 JS ，所以此处才把对应的 JS 提前引入
import 'codemirror/mode/yaml/yaml.js';

// 尝试获取全局实例
const codemirror = window.CodeMirror || CodeMirror;

/**
 * 初始化
 * @param textarea
 * @param readonly
 * @param onChange
 * @returns CodeMirror
 */
function initialize(textarea, readonly, onChange) {
  const instance = codemirror.fromTextArea(textarea, {
    // 模式
    mode: 'yaml',
    // 缩进格式
    tabSize: 2,
    // 主题，对应主题库 JS 需要提前引入
    theme: 'ayu-mirage',
    // 强制换行
    lineWrapping: true,
    // 显示行号
    lineNumbers: true,
    // 是否只读
    readOnly: readonly,
    // 快捷键
    extraKeys: {
      // 注释
      [Comment.shortcut]: Comment.command,
      // 制表符
      [Tab.shortcut]: Tab.command,
    },
  });

  if (!readonly) {
    // 绑定change事件
    instance.on('change', onChange);
  }

  // 解决初始化时代码盖住行号问题
  setTimeout(() => instance.refresh());

  return instance;
}

export default defineComponent({
  name: 'jm-dsl-editor',
  props: {
    value: String,
    readonly: Boolean,
  },
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance();
    const { toClipboard } = useClipboard();
    const textareaRef = ref(null);

    let instance;
    // 禁止外部编辑
    let externalEditingForbidden = false;
    const handleChange = inst => {
      // 开始编辑后，禁止外部编辑
      externalEditingForbidden = true;

      // Copy the content of the editor into the textarea.
      inst.save();
      emit('update:value', textareaRef.value.value);
    };

    onMounted(() => {
      // 设置textarea值
      textareaRef.value.value = props.value;

      // 初始化
      instance = initialize(textareaRef.value, props.readonly, handleChange);
    });

    onBeforeUpdate(() => {
      if (externalEditingForbidden) {
        // 禁止外部编辑
        return;
      }

      // Remove the editor, and restore the original textarea
      instance.toTextArea();

      // 更新textarea值
      textareaRef.value.value = props.value;

      // 重新初始化
      instance = initialize(textareaRef.value, props.readonly, handleChange);
    });

    return {
      textareaRef,
      clickEmpty: e => {
        if (!e.target.className.includes('scrollbar__wrap')) {
          // 只有点击scrollbar元素时，生效
          return;
        }

        instance.focus();
        // Set the cursor at the end of existing content
        instance.setCursor(instance.lineCount(), 0);
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
.jm-dsl-editor {
  position: relative;
  z-index: 0;
  // 必须与codemirror主题背景保持一致
  background-color: #19253B;
  height: 100%;

  &:hover {
    .operation {
      visibility: visible;
    }
  }

  .operation {
    position: absolute;
    right: 20px;
    bottom: 20px;
    z-index: 1;

    display: flex;
    align-items: center;
    padding: 10px 15px;
    background-color: #818894;
    box-shadow: 0 0 4px 0 rgba(194, 194, 194, 0.5);
    border-radius: 2px;
    border: 1px solid #767F91;

    visibility: hidden;

    .copy {
      width: 24px;
      height: 24px;
      background-image: url('./svgs/copy.svg');
      background-repeat: no-repeat;
      background-size: contain;
      cursor: pointer;
    }
  }

  ::v-deep(.CodeMirror) {
    z-index: 0;
    height: inherit;
    background-color: #19253B;

    * {
      // 字体设为等宽
      font-family: Menlo, Monaco, 'Courier New', monospace;
      line-height: 20px;
    }

    .CodeMirror-gutters {
      z-index: 0;
      background-color: #19253B;
    }

    .CodeMirror-linenumber {
      color: #C1D7FF;
    }
  }
}
</style>