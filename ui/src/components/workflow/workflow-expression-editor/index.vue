<template>
  <div class="jm-workflow-expression-editor">
    <param-button :selectableParams="selectableParams" @inserted="handleInserted"/>
    <div class="top">
      <div class="exp-lang">语法：{{ expLang }}</div>
      <div class="param-type" v-show="paramTypeVisible">{{ paramTypeTxt }}</div>
    </div>
    <textarea ref="textareaRef"></textarea>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, nextTick, onMounted, PropType, ref } from 'vue';
import { ExpressionEditor } from './model/expression-editor';
import { ISelectableParam } from './model/data';
import ParamButton from './param-button.vue';
import { ParamTypeEnum } from '../workflow-editor/model/data/enumeration';

export default defineComponent({
  name: 'jm-workflow-expression-editor',
  components: { ParamButton },
  props: {
    modelValue: {
      type: String,
      default: '',
    },
    expLang: {
      type: String,
      default: 'js/ts',
    },
    paramType: {
      type: String as PropType<ParamTypeEnum>,
      required: true,
    },
    placeholder: {
      type: String,
      default: '',
    },
    selectableParams: {
      type: Array as PropType<ISelectableParam[]>,
      required: true,
    },
  },
  emits: ['update:model-value', 'focus', 'blur', 'change'],
  setup(props, { emit }) {
    const textareaRef = ref<HTMLTextAreaElement>();
    const paramTypeVisible = ref<boolean>(false);
    let expressionEditor: ExpressionEditor;

    const syncValue = () => {
      const val = textareaRef.value!.value;
      emit('update:model-value', val);
      emit('change', val);
    };

    onMounted(async () => {
      const textareaEl = textareaRef.value!;
      textareaEl.value = props.modelValue;

      // 保证textarea值正确渲染
      await nextTick();

      expressionEditor = new ExpressionEditor(textareaEl, props.placeholder,
        e => {
          paramTypeVisible.value = true;
          emit('focus', e);
        }, e => {
          syncValue();
          paramTypeVisible.value = false;
          emit('blur', e);
        });
    });

    return {
      textareaRef,
      paramTypeVisible,
      paramTypeTxt: computed<string>(() => {
        let str = '参数类型：';
        switch (props.paramType) {
          case ParamTypeEnum.STRING:
            str += '字符串';
            break;
          case ParamTypeEnum.NUMBER:
            str += '数字';
            break;
          case ParamTypeEnum.BOOL:
            str += '布尔';
            break;
          default:
            str += '未知';
            break;
        }
        return str;
      }),
      handleInserted: (arr: string[]) => {
        expressionEditor.insertParam(arr);
        syncValue();
      },
    };
  },
});
</script>

<style scoped lang="less">
@import "../workflow-editor/vars";

.jm-workflow-expression-editor {
  position: relative;

  .param-button {
    position: absolute;
    top: -25px;
    right: 0;
  }

  .top {
    box-sizing: border-box;
    width: calc(100% - 2px);
    padding: 0 14px;
    height: 24px;
    position: absolute;
    top: 1px;
    left: 1px;
    z-index: 1;
    background-color: #ECF2F8;
    border-radius: 1px 1px 0 0;
    color: #7B8C9C;
    font-size: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .exp-lang {

    }

    .param-type {

    }
  }

  ::v-deep(.CodeMirror) {
    height: inherit;

    * {
      // 字体设为等宽
      font-family: Menlo, Monaco, 'Courier New', monospace;
    }

    .CodeMirror-gutters {
      background-color: transparent;
      border-right-color: transparent;
    }

    .CodeMirror-linenumber {
      font-size: 12px;
      color: #8193B2;
    }

    .CodeMirror-lines {
      padding: 0;
    }

    pre.CodeMirror-line, pre.CodeMirror-line-like {
      padding: 0;
    }

    &.CodeMirror-focused {
      box-sizing: border-box;
      border-color: @primary-color;
    }

    .CodeMirror-placeholder {
      color: #c0c4cc;
    }

    line-height: 26px;
    min-height: 36px;
    background-color: #FFFFFF;

    &:hover {
      border-color: @primary-color;
    }

    transition: all .1s ease-in-out;

    color: #333333;
    border-radius: 2px;
    border: 1px solid #D0E0ED;
    padding-right: 15px;
    padding-top: 24px;
    box-sizing: border-box;
  }
}
</style>