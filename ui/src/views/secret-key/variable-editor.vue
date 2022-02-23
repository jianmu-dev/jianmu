<template>
  <div class="secret-key-variable-editor">
    <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef" :inline="true" class="form" @submit.prevent>
      <jm-form-item label="变量名" prop="name">
        <jm-input v-model="editorForm.name" clearable :disabled="disabled" placeholder="请输入变量名" class="item-input"/>
      </jm-form-item>
      <jm-form-item label="变量值" prop="value">
        <jm-input :type="disabled? 'password' : 'textarea'" v-model="editorForm.value" clearable :disabled="disabled"
                  :autosize="{ minRows: 1}" resize="none"
                  placeholder="请输入变量值" class="item-input"/>
      </jm-form-item>
    </jm-form>
    <div class="operation">
      <jm-button v-if="!disabled" type="text" @click="save" :loading="savingLoading">保存</jm-button>
      <jm-button type="text" @click="del" :loading="deletingLoading" @keypress.enter.prevent>删除</jm-button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref, SetupContext } from 'vue';
import { createSecretKey, deleteSecretKey } from '@/api/secret-key';

export interface IVariableType {
  name: string;
  value: string;
}

const defaultValue = '**************************************************';

export default defineComponent({
  props: {
    index: {
      type: Number,
      required: true,
    },
    ns: {
      type: String,
      required: true,
    },
    name: String,
    validator: Function,
  },
  emits: ['saved', 'deleted'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;

    const disabled = ref<boolean>(!!props.name);
    const editorFormRef = ref<any>(null);
    const editorForm = ref<IVariableType>({
      name: props.name,
      value: disabled.value ? defaultValue : '',
    });
    const editorRule = ref<object>({
      name: [
        { required: true, message: '变量名不能为空', trigger: 'blur' },
      ],
      value: [
        { required: true, message: '变量值不能为空', trigger: 'blur' },
      ],
    });
    const savingLoading = ref<boolean>(false);
    const deletingLoading = ref<boolean>(false);

    return {
      disabled,
      editorFormRef,
      editorForm,
      editorRule,
      savingLoading,
      deletingLoading,
      save: () => {
        savingLoading.value = true;

        editorFormRef.value.validate((valid: boolean) => {
          if (!valid) {
            // 关闭loading
            savingLoading.value = false;

            return false;
          }

          if (props.validator) {
            const message = props.validator({ ...editorForm.value }) as string;
            if (message) {
              proxy.$error(message);

              // 关闭loading
              savingLoading.value = false;

              return false;
            }
          }

          createSecretKey({
            namespace: props.ns,
            key: editorForm.value.name,
            value: editorForm.value.value,
          }).then(() => {
            proxy.$success('保存成功');

            emit('saved', props.index, props.ns, { ...editorForm.value });

            disabled.value = true;
            editorForm.value.value = defaultValue;
          }).catch((err: Error) => {
            // 关闭loading
            savingLoading.value = false;

            proxy.$throw(err, proxy);
          });
        });
      },
      del: () => {
        if (!disabled.value) {
          emit('deleted', props.index);
          return;
        }

        deletingLoading.value = true;

        proxy.$confirm('确定要删除吗?', '删除密钥', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(() => {
          deleteSecretKey(props.ns, editorForm.value.name).then(() => {
            proxy.$success('删除成功');

            emit('deleted', props.index);
          }).catch((err: Error) => {
            // 关闭loading
            deletingLoading.value = false;

            proxy.$throw(err, proxy);
          });
        }).catch(() => {
          // 关闭loading
          deletingLoading.value = false;
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.secret-key-variable-editor {
  width: 950px;
  font-size: 14px;
  color: #333333;
  display: flex;
  align-items: center;

  .form {
    padding: 6px 24px;
    background-color: rgba(238, 240, 243, 0.5);
    border-radius: 4px;

    ::v-deep(.el-form-item) {
      margin-bottom: 0;

      .el-form-item__label {
        line-height: 33px;
      }

      .el-form-item__content {
        line-height: normal;

        .el-input {
          line-height: 33px;

          .el-input__inner {
            height: 33px;
          }
        }
      }
    }

    .item-input {
      width: 300px;
    }
  }

  .operation {
    padding: 0 24px;
  }
}
</style>
