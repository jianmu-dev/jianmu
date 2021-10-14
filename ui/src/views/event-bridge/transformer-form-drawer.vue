<template>
  <div>
    <jm-drawer
      title="设置转换器"
      size="45%"
      :model-value="modelValue"
      destroy-on-close
      @update:model-value="changeDialogVisible"
      direction="rtl">
      <div class="transformer-form-drawer">
        <jm-scrollbar>
          <div class="main">
            <div class="template-dropdown">
              <jm-dropdown>
                <span class="el-dropdown-link">
                  选择转换器模板<i class="el-icon-caret-bottom el-icon--right"></i>
                </span>
                <template #dropdown>
                  <jm-dropdown-menu>
                    <jm-dropdown-item v-for="name in transformerTemplateNames" :key="name"
                                      @click="selectTemplate(name)">{{ name }}
                    </jm-dropdown-item>
                  </jm-dropdown-menu>
                </template>
              </jm-dropdown>
            </div>
            <jm-form label-position="top" :model="editorForm" ref="editorFormRef"
                     @submit.prevent>
              <div class="transformers">
                <div class="transformer" v-for="(transformer, index) in editorForm.transformers"
                     :key="uuidv4() + index">
                  <div class="del-btn" @click="del(index)"></div>
                  <div class="line">
                    <jm-form-item label="转换器类型" :prop="`transformers.${index}.type`" :rules="editorRule.type">
                      <jm-select v-model="transformer.type" clearable placeholder="请选择转换器类型">
                        <jm-option v-for="transformerType in transformerTypes"
                                   :key="transformerType" :label="transformerType" :value="transformerType"/>
                      </jm-select>
                    </jm-form-item>
                    <jm-form-item label="转换器表达式" :prop="`transformers.${index}.expression`"
                                  :rules="editorRule.expression">
                      <jm-input v-model="transformer.expression" clearable placeholder="请输入转换器表达式"/>
                    </jm-form-item>
                  </div>
                  <div class="line">
                    <jm-form-item label="变量类型" :prop="`transformers.${index}.variableType`"
                                  :rules="editorRule.variableType">
                      <jm-select v-model="transformer.variableType" clearable placeholder="请选择变量类型">
                        <jm-option v-for="parameterType in parameterTypes"
                                   :key="parameterType" :label="parameterType" :value="parameterType"/>
                      </jm-select>
                    </jm-form-item>
                    <jm-form-item label="变量名" :prop="`transformers.${index}.variableName`"
                                  :rules="editorRule.variableName">
                      <jm-input v-model="transformer.variableName" clearable placeholder="请输入变量名"/>
                    </jm-form-item>
                  </div>
                </div>
              </div>
            </jm-form>
            <div class="add-btn">
              <jm-button type="text" icon="jm-icon-link-add" @click="add">新增输入参数</jm-button>
            </div>
          </div>
        </jm-scrollbar>
        <div class="btn-area">
          <jm-button class="jm-icon-button-cancel" size="small" @click="changeDialogVisible(false)">取消</jm-button>
          <jm-button type="primary" class="jm-icon-button-preserve" size="small" @click="preserve">保存</jm-button>
        </div>
      </div>
    </jm-drawer>
  </div>
</template>

<script lang="ts">
import { defineComponent, onBeforeMount, ref, SetupContext, watch } from 'vue';
import { IEventBridgeTargetTransformerVo } from '@/api/dto/event-bridge';
import { EventBridgeTargetTransformerTypeEnum } from '@/api/dto/enumeration';
import {
  fetchEventBridgeTargetTransformerTemplate,
  fetchEventBridgeTargetTransformerTemplateName,
  fetchParameterType,
} from '@/api/view-no-auth';
import { v4 as uuidv4 } from 'uuid';

export default defineComponent({
  props: {
    modelValue: {
      type: Boolean,
      required: true,
    },
    transformers: {
      type: Array,
      required: true,
    },
  },
  emits: ['update:modelValue', 'update:transformers', 'confirmed'],
  setup(props: any, { emit }: SetupContext) {
    const editorFormRef = ref<any>(null);
    const editorForm = ref<{
      transformers: {
        type: EventBridgeTargetTransformerTypeEnum | '';
        variableName: string;
        variableType: string;
        expression: string;
      }[]
    }>({
      transformers: [],
    });
    const editorRule = ref<{
      type: any[];
      expression: any[];
      variableType: any[];
      variableName: any[];
    }>({
      type: [
        { required: true, message: '请选择转换器类型', trigger: 'change' },
      ],
      expression: [
        { required: true, message: '请输入转换器表达式', trigger: 'blur' },
      ],
      variableType: [
        { required: true, message: '请选择变量类型', trigger: 'change' },
      ],
      variableName: [
        { required: true, message: '请输入变量名', trigger: 'blur' },
      ],
    });
    const transformerTemplateNames = ref<string[]>([]);
    const parameterTypes = ref<string[]>([]);

    onBeforeMount(async () => {
      transformerTemplateNames.value = await fetchEventBridgeTargetTransformerTemplateName();
      parameterTypes.value = await fetchParameterType();
    });

    const changeDialogVisible = (newValue: boolean) => {
      emit('update:modelValue', newValue);
    };

    watch(
      () => props.transformers,
      (newValue: IEventBridgeTargetTransformerVo[]) =>
        // 防止引用传递导致数据污染，构造新引用
        (editorForm.value.transformers = JSON.parse(JSON.stringify(newValue))),
    );

    return {
      uuidv4,
      editorFormRef,
      editorForm,
      editorRule,
      transformerTypes: Object.values(EventBridgeTargetTransformerTypeEnum),
      transformerTemplateNames,
      parameterTypes,
      changeDialogVisible,
      selectTemplate: async (name: string) => {
        editorForm.value.transformers = await fetchEventBridgeTargetTransformerTemplate(name);
      },
      add: () => {
        editorForm.value.transformers.push({
          type: '',
          variableName: '',
          variableType: '',
          expression: '',
        });
      },
      del: (index: number) => {
        editorForm.value.transformers.splice(index, 1);
      },
      preserve: () => {
        editorFormRef.value.validate((valid: boolean) => {
          if (!valid) {
            return false;
          }

          emit('update:transformers', editorForm.value.transformers);
          emit('confirmed');

          changeDialogVisible(false);
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.transformer-form-drawer {
  height: calc(100vh - 128px);
  position: relative;

  .main {
    margin: 20px;
    border: 1px solid #B9CFE6;

    .template-dropdown {
      padding: 15px 20px;
      display: flex;
      justify-content: flex-end;
      border-bottom: 1px solid #E6EBF2;

      .el-dropdown-link {
        cursor: pointer;
        color: #096DD9;
      }
    }

    .transformers {
      > :nth-child(2n + 1) {
        background-color: #F5F8FB;
      }

      .transformer {
        position: relative;
        padding: 10px 20px;
        border-bottom: 1px solid #E6EBF2;

        .del-btn {
          position: absolute;
          top: 15px;
          right: 15px;
          width: 16px;
          height: 16px;
          background-image: url('@/assets/svgs/btn/del2.svg');
          background-repeat: no-repeat;
          background-size: contain;
          cursor: pointer;
        }

        .line {
          display: flex;
          justify-content: space-between;
        }
      }
    }

    .add-btn {
      margin: 5px 20px;
    }

    ::v-deep(.el-form) {
      .el-form-item + .el-form-item {
        margin-left: 20px;
      }

      .el-form-item {
        width: 50%;

        .el-form-item__label {
          //line-height: 35px;
          padding-bottom: 0;
        }

        .el-form-item__content {
          .el-select {
            width: 100%;
          }
        }
      }
    }
  }

  .btn-area {
    height: 60px;
    position: absolute;
    right: 0;
    bottom: -60px;
    padding: 0 20px;
    width: 100%;
    box-sizing: border-box;
    border-top: 1px solid #E8E8E8;
    display: flex;
    justify-content: flex-end;
    align-items: center;

    .el-button + .el-button {
      margin-left: 20px;
    }
  }
}
</style>