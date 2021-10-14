<template>
  <div class="param-editor">
    <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef">
      <jm-form-item label="参数唯一标识" label-position="top" prop="ref">
        <jm-input v-model="editorForm.ref" clearable placeholder="请输入参数Ref"/>
      </jm-form-item>
      <jm-form-item label="参数名称" label-position="top" prop="name">
        <jm-input v-model="editorForm.name" clearable placeholder="请输入参数名称"/>
      </jm-form-item>
      <jm-form-item label="参数类型" label-position="top" prop="type">
        <jm-select v-model="editorForm.type">
          <jm-option v-for="item in parameterTypes" :key="item" :label="item" :value="item"/>
        </jm-select>
      </jm-form-item>
      <jm-form-item label="参数值" label-position="top" prop="value">
        <jm-input v-model="editorForm.value" clearable placeholder="请输入参数值"/>
      </jm-form-item>
      <jm-form-item label="参数描述" label-position="top" prop="description">
        <jm-input type="textarea" v-model="editorForm.description" clearable placeholder="请输入参数描述"/>
      </jm-form-item>
    </jm-form>
    <div class="btn-area">
      <jm-button type="primary" class="jm-icon-button-preserve" size="small" @click="save">保存</jm-button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref, SetupContext } from 'vue';
import { Mutable } from '@/utils/lib';
import { ITaskParameterVo } from '@/api/dto/task-definition';
import { useStore } from 'vuex';
import { IRootState } from '@/model';

export default defineComponent({
  props: {
    type: {
      type: String,
      required: true,
      validator: value => ['input', 'output'].includes(value as string),
    },
    // 当前refs
    currentRefs: {
      type: Array,
      required: true,
    },
  },
  emits: ['on-save'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const { parameterTypes } = useStore().state as IRootState;

    const editorFormRef = ref<any>(null);
    const editorForm = ref<Mutable<ITaskParameterVo>>({
      ref: '',
      name: '',
      type: parameterTypes[0],
      value: '',
    });
    const editorRule = ref<object>({
      ref: [
        { required: true, message: '参数Ref不能为空', trigger: 'blur' },
      ],
      name: [
        { required: true, message: '参数名称不能为空', trigger: 'blur' },
      ],
      type: [
        { required: true, message: '参数类型不能为空', trigger: 'change' },
      ],
      value: [
        { required: true, message: '参数值不能为空', trigger: 'change' },
      ],
    });

    return {
      parameterTypes,
      editorFormRef,
      editorForm,
      editorRule,
      save: () => {
        editorFormRef.value.validate((valid: boolean) => {
          if (!valid) {
            return false;
          }

          if (props.currentRefs.includes(editorForm.value.ref)) {
            // 检查ref是否重复
            proxy.$error('参数Ref重复');

            return;
          }

          emit('on-save', props.type, { ...editorForm.value });
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.param-editor {
  padding: 15px 24px 0;
  height: 100%;
  position: relative;

  ::v-deep(.el-form-item) {
    margin-bottom: 15px;

    .el-form-item__label {
      line-height: 30px;
      float: none;
    }
  }

  .btn-area {
    padding: 14px 24px;
    position: absolute;
    right: 0;
    bottom: 0;
    width: 100%;
    border-top: 1px solid #E8E8E8;
    text-align: right;
  }
}
</style>
