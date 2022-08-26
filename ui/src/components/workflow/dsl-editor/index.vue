<template>
  <div class="jm-dsl-editor" @click="clickEmpty">
    <div v-if="readonly" class="operation">
      <jm-text-copy :value="value"/>
    </div>
    <textarea ref="textareaRef"></textarea>
  </div>
</template>

<script type="text/ecmascript-6">
import { defineComponent, onBeforeUpdate, onMounted, ref } from 'vue';
// 引入全局实例
import CodeMirror from 'codemirror';
import { Comment, Find, Tab } from './model/shortcut';

// 核心样式
import 'codemirror/lib/codemirror.css';
// 引入主题后，还需要在 options 中指定主题才会生效
import 'codemirror/theme/ayu-mirage.css';

// 需要引入具体的语法高亮库才会有对应的语法高亮效果
// codemirror 官方其实支持通过 /addon/mode/loadmode.js 和 /mode/meta.js 来实现动态加载对应语法高亮库
// 但 vue 貌似没有无法在实例初始化后再动态加载对应 JS ，所以此处才把对应的 JS 提前引入
import 'codemirror/mode/yaml/yaml.js';

// 查找插件
import './addon/search.js';

// 滚动条插件
import 'codemirror/addon/scroll/simplescrollbars.js';
import 'codemirror/addon/scroll/simplescrollbars.css';

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
    // 滚动条样式
    scrollbarStyle: 'overlay',
    // 是否只读
    readOnly: readonly,
    // 快捷键
    extraKeys: {
      // 注释
      [Comment.shortcut]: Comment.command,
      // 制表符
      [Tab.shortcut]: Tab.command,
      // 查找
      [Find.shortcut]: Find.command,
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
    };
  },
});
</script>

<style scoped lang="less">
@thumb-color: #667085;

.jm-dsl-editor {
  position: relative;
  z-index: 0;
  // 必须与codemirror主题背景保持一致
  background-color: #19253B;
  height: 100%;

  &:hover {
    .operation {
      opacity: 1;
    }
  }

  .operation {
    position: absolute;
    right: 10px;
    top: 10px;
    z-index: 5;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 30px;
    background-color: #818894;
    box-shadow: 0 0 4px 0 rgba(194, 194, 194, 0.5);
    border-radius: 2px;

    opacity: 0;
    transition: .3s opacity;

    ::v-deep(.jm-text-copy) {
      font-size: 20px;

      .jm-icon-button-copy {
        color: #FFFFFF;
        opacity: 1;
      }
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

    &:hover {
      .CodeMirror-overlayscroll-horizontal,
      .CodeMirror-overlayscroll-vertical {
        opacity: 1;
      }
    }

    .CodeMirror-overlayscroll-horizontal,
    .CodeMirror-overlayscroll-vertical {
      opacity: 0;
      transition: .3s opacity;

      div {
        background-color: @thumb-color;
        opacity: 0.5;

        &:hover {
          opacity: 1;
          cursor: pointer;
        }
      }
    }
  }
}
</style>
