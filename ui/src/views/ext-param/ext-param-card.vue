<template>
  <div class="ext-param-card" @mouseover="over" @mouseout="leave">
    <div class="ext-ref">
      <div :style="{width:wrapperWidth}" class="wrapper">
        <div class="ref-value" :style="{maxWidth:`${width}`}">
          <jm-text-viewer :value="reference" :tip-append-to-body="false"
                          :threshold="0"
                          @loaded="({contentMaxWidth})=>getMaxWidth(contentMaxWidth)"/>
        </div>
        <jm-tooltip content="唯一标识" placement="top">
          <i class="ref-icon"></i>
        </jm-tooltip>
      </div>
      <!--  删除/编辑   -->
      <span class="editor-btn">
          <jm-tooltip content="编辑" placement="top">
            <i class="jm-icon-workflow-edit" @click="editor"></i>
          </jm-tooltip>
          <jm-tooltip content="删除" placement="top">
          <i class="jm-icon-button-delete" @click="del"></i>
          </jm-tooltip>
        </span>
    </div>
    <div class="ext-name">
      <div class="label">名称：</div>
      <div class="value">
        <jm-text-viewer class="name-value" :value="name" :tip-append-to-body="false"/>
      </div>
    </div>
    <div class="ext-value">
      <div class="label">值：</div>
      <div class="value">
        <jm-text-viewer :value="value" :tip-append-to-body="false"/>
      </div>
    </div>
    <div class="ext-label">
      <div
        :class="{
            'tag':true,
            'string':type === ParamTypeEnum.STRING,
            'number':type === ParamTypeEnum.NUMBER,
            'boolean':type === ParamTypeEnum.BOOL}">
        {{ typeTxt }}
      </div>
      <div class="custom-tag">{{ label }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { ParamTypeEnum } from '@/api/dto/enumeration';


export default defineComponent({
  props: {
    reference: {
      type: String,
    },
    name: {
      type: String,
    },
    type: {
      type: String as PropType<ParamTypeEnum>,
    },
    value: {
      type: String,
    },
    label: {
      type: String,
    },
    id: {
      type: String,
    },
  },
  emits: ['editor', 'delete'],
  setup(props, { emit }) {
    let typeTxt: string;
    if (props.type === ParamTypeEnum.BOOL) {
      typeTxt = '布尔';
    } else if (props.type === ParamTypeEnum.NUMBER) {
      typeTxt = '数字';
    } else if (props.type === ParamTypeEnum.STRING) {
      typeTxt = '字符串';
    }
    const width = ref<string>('10px');
    const getMaxWidth = (w: number) => {
      width.value = w + 'px';
    };
    const wrapperWidth = ref<string>('100%');
    return {
      wrapperWidth,
      width,
      typeTxt,
      ParamTypeEnum,
      editor: () => {
        emit('editor', props.id);
      },
      del: () => {
        emit('delete', props.id, props.name);
      },
      getMaxWidth,
      over() {
        wrapperWidth.value = 'calc(100% - 60px)';
      },
      leave() {
        wrapperWidth.value = '100%';
      },
    };
  },
});
</script>
<style scoped lang="less">
.ext-param-card {
  width: 19.2%;
  margin: .8% .385% 0 .385%;
  box-sizing: border-box;
  padding: 20px;
  font-size: 14px;
  height: 180px;
  background: #FFFFFF;
  border-radius: 4px;
  border: 1px solid #E7ECF1;
  position: relative;

  &:hover {
    background: #FFFFFF;
    box-shadow: 0 0 12px 4px #EDF1F8;
    border-radius: 4px;
    border: 1px solid transparent;
  }

  &:hover .ext-ref .editor-btn {
    display: flex;
  }

  .ext-ref {
    height: 22px;
    font-size: 16px;
    color: #082340;
    line-height: 22px;
    padding-bottom: 16px;
    margin-bottom: 15px;
    border-bottom: 1px solid #E7ECF1;
    display: flex;
    align-items: center;

    .wrapper {
      display: flex;
      align-items: center;

      .ref-icon {
        display: inline-block;
        width: 16px;
        height: 16px;
        background-image: url("@/assets/svgs/ext-param/ref-icon.svg");
      }

      .ref-value {
        width: 90%;
      }
    }

    .editor-btn {
      display: none;

      .jm-icon-workflow-edit::before,
      .jm-icon-button-delete::before {
        width: 26px;
        height: 26px;
        line-height: 26px;
        font-size: 18px;
        color: #6B7B8D;
        border-radius: 2px;
        background: rgba(255, 255, 255, .9);
      }

      .jm-icon-workflow-edit:hover,
      .jm-icon-button-delete:hover {
        &::before {
          background: #EFF7FF;
          cursor: pointer;
        }
      }
    }
  }

  .ext-name,
  .ext-value {
    display: flex;
    align-items: center;
    height: 22px;
    font-weight: 400;
    color: #082340;
    line-height: 18px;

    .value {
      width: 75%;
    }

    .name-value {
      line-height: 20px;
    }
  }

  .ext-value {
    .value {
      width: 85%;
    }
  }

  .ext-value {
    margin: 10px 0;
  }

  // 不同类型不同色块
  .ext-label {
    font-size: 12px;
    display: flex;
    height: 20px;
    line-height: 15px;

    .tag,
    .custom-tag {
      padding: 4px;
      box-sizing: border-box;
    }

    .tag {
      background: #E6F7FF;
      border-radius: 2px;
      width: 44px;
      text-align: center;
    }

    .custom-tag {
      max-width: 70%;
      margin-left: 10px;
      background: #E6F7FF;
      color: #096DD9;
      overflow: hidden;
    }

    .string {
      background: #FFF7E6;
      color: #6D4C41;
    }

    .number {
      background: #E6FFFB;
      color: #006D75;
    }

    .boolean {
      background: #F2EEFF;
      color: #6236FF;
    }
  }
}
</style>
