<template>
  <div class="webhook-setting-dialog">
    <jm-dialog
      :model-value="dialogVisible"
      @update:model-value="e => $emit('update:model-value', e)"
      width="820px"
      :destroy-on-close="true"
      @close="closeDialog"
    >
      <template #title> 触发执行</template>
      <div class="content-wrapper" v-loading="loading">
        <div class="operator" v-if="paramsTableData.length > 0">
          <span class="tips">项目已配置webhook，手动触发请填写参数</span>
          <div class="btns">
            <div class="import" v-if="latestWebhook" @click.stop="importWebhookDefinition">
              <i class="icon jm-font jm-icon-button-import" />
              <span>填入上次参数</span>
            </div>
            <div class="clear" @click="clearFields">
              <i class="icon jm-font jm-icon-button-clear" />
              <span>清空表单</span>
            </div>
          </div>
        </div>
        <div class="content-container" v-if="paramsTableData.length > 0">
          <jm-form :rules="rules" :model="ruleForm" ref="formRef" @submit.prevent>
            <div ref="paramTableRef">
              <jm-table
                :data="paramsTableData"
                :header-row-class-name="'header-row'"
                :height="paramsTableHeight"
                class="params-table"
              >
                <jm-table-column prop="name" label="参数名" class-name="webhook-param-name" width="256px">
                  <template #default="scope">
                    <div class="param-name">
                      <i v-if="scope.row.required" />
                      <span>{{ scope.row.name }}</span>
                    </div>
                  </template>
                </jm-table-column>
                <jm-table-column prop="type" label="类型" width="160px">
                  <template #default="scope">
                    {{ scope.row.type }}
                  </template>
                </jm-table-column>
                <jm-table-column prop="exp" label="参数值" class-name="webhook-param-value">
                  <template #default="scope">
                    <jm-form-item v-if="scope.row.type === ParamTypeEnum.BOOL" :prop="scope.row.name">
                      <jm-radio-group v-model="ruleForm[scope.row.name]">
                        <jm-radio :label="true">true</jm-radio>
                        <jm-radio :label="false">false</jm-radio>
                      </jm-radio-group>
                    </jm-form-item>
                    <jm-form-item v-if="scope.row.type === ParamTypeEnum.NUMBER" :prop="scope.row.name">
                      <jm-input v-model.number="ruleForm[scope.row.name]" type="number" placeholder="请输入参数值" />
                    </jm-form-item>
                    <jm-form-item v-if="scope.row.type === ParamTypeEnum.STRING" :prop="scope.row.name">
                      <jm-input
                        v-model="ruleForm[scope.row.name]"
                        type="textarea"
                        placeholder="请输入参数值"
                        :autosize="{
                          minRows: 1,
                          maxRows: 3,
                        }"
                        :resize="'none'"
                      ></jm-input>
                    </jm-form-item>
                    <jm-form-item
                      v-if="scope.row.type === ParamTypeEnum.SECRET"
                      :prop="scope.row.name"
                      class="secret-item"
                    >
                      <jm-input
                        v-model="ruleForm[scope.row.name]"
                        placeholder="请输入参数值"
                        type="password"
                        show-password
                      ></jm-input>
                    </jm-form-item>
                  </template>
                </jm-table-column>
              </jm-table>
            </div>
          </jm-form>
          <div class="auth-wrapper" v-if="authTableData.length > 0">
            <div class="title">认证规则</div>
            <jm-table class="auth-table" :data="authTableData" :header-row-class-name="'header-row'">
              <jm-table-column prop="token" label="token" />
              <jm-table-column prop="value" label="value" />
            </jm-table>
          </div>
          <div class="only-wrapper" v-if="onlyExpression">
            <div class="title">only</div>
            <div class="only">{{ onlyExpression }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <span>
          <jm-button size="small" @click="closeDialog">取消</jm-button>
          <jm-button size="small" type="primary" @click="confirm">确定</jm-button>
        </span>
      </template>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onMounted, ref, PropType, nextTick } from 'vue';
import { ElForm } from 'element-plus';
import { IProjectTriggeringDto, ITriggerDefinitionVo, IWebhookAuth, IWebhookParameter } from '@/api/dto/project';
import { ParamTypeEnum } from '@/components/workflow/workflow-editor/model/data/enumeration';
import { IWebhookParameterVo, IWebRequestVo } from '@/api/dto/trigger';
import { getWebhookList, getWebhookParams } from '@/api/trigger';

export default defineComponent({
  emits: ['update:model-value', 'submit'],
  props: {
    modelValue: {
      type: Boolean,
      required: true,
    },
    projectId: {
      type: String,
      required: true,
    },
    webhookDefinition: {
      type: Object as PropType<ITriggerDefinitionVo>,
      required: true,
    },
  },
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = computed<boolean>(() => props.modelValue);
    const formRef = ref<InstanceType<typeof ElForm>>();
    const loading = ref<boolean>(false);
    // 参数table数据
    const paramsTableData = ref<IWebhookParameter[]>([]);
    // auth table数据
    const authTableData = ref<IWebhookAuth[]>([]);
    // only table数据
    const onlyExpression = ref<string>('');
    const rules = ref<Record<string, any>>({});
    const ruleForm = ref<Record<string, any>>({});
    const latestWebhook = ref<IWebRequestVo>();
    // 获取最近执行的webhook记录
    const getLatestWebhook = async () => {
      const webhookList = await getWebhookList({
        projectId: props.projectId,
        pageNum: 1,
        pageSize: 1,
      });
      latestWebhook.value = webhookList.list.length !== 0 ? webhookList.list[0] : undefined;
    };
    const paramTableRef = ref<HTMLElement>();
    const paramsTableHeight = ref<number>();
    // 初始化数据
    const init = async () => {
      try {
        await nextTick();
        loading.value = true;
        paramsTableData.value = props.webhookDefinition.params!;

        if (props.webhookDefinition.auth) {
          authTableData.value.push(props.webhookDefinition.auth);
        }

        if (props.webhookDefinition.only) {
          onlyExpression.value = props.webhookDefinition.only;
        }
        paramsTableData.value!.forEach((param: IWebhookParameter) => {
          if (param.required) {
            const isBoolean = param.type === ParamTypeEnum.BOOL;
            const rule = [
              {
                required: true,
                message: isBoolean ? '请选择参数值' : '请输入参数值',
                trigger: isBoolean ? 'change' : 'blur',
              },
            ];
            rules.value[param.name] = rule;
          }
          ruleForm.value[param.name] = '';
          if (!param.required) {
            // 对非必填的赋默认值
            ruleForm.value[param.name] = param.defaultValue;
          }
        });
        await getLatestWebhook();
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };
    // 导入上一次webhook触发器定义
    const importWebhookDefinition = async () => {
      try {
        loading.value = true;
        const webhookDefinition = await getWebhookParams(latestWebhook.value!.id);
        if (!webhookDefinition.param) {
          return;
        }

        if (webhookDefinition.auth) {
          authTableData.value.push(webhookDefinition.auth);
        }

        if (webhookDefinition.only) {
          onlyExpression.value = webhookDefinition.only;
        }

        webhookDefinition.param.forEach((param: IWebhookParameterVo) => {
          if (paramsTableData.value.find(({ name }) => name === param.name)) {
            ruleForm.value[param.name] = param.value;
          }
        });
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };
    onMounted(async () => {
      await init();
      if (paramTableRef.value!.offsetHeight >= 378) {
        paramsTableHeight.value = 378;
      }
    });
    // 清空表单
    const clearFields = () => {
      formRef.value!.resetFields();
      ruleForm.value = {};
    };
    // 关闭弹窗
    const closeDialog = () => {
      emit('update:model-value', false);
      clearFields();
    };
    // 确认提交
    const confirm = () => {
      formRef.value
        ?.validate()
        .then(() => {
          const payload = {
            triggerParams: [],
          } as IProjectTriggeringDto;
          Object.keys(ruleForm.value).forEach((key: string) => {
            payload.triggerParams.push({ name: key, value: ruleForm.value[key] });
          });
          emit('submit', payload);
        })
        .catch(err => {
          console.warn(err);
        });
    };
    return {
      paramTableRef,
      paramsTableHeight,
      ruleForm,
      loading,
      dialogVisible,
      formRef,
      closeDialog,
      confirm,
      rules,
      paramsTableData,
      authTableData,
      onlyExpression,
      clearFields,
      ParamTypeEnum,
      latestWebhook,
      importWebhookDefinition,
    };
  },
});
</script>

<style scoped lang="less">
.webhook-setting-dialog {
  ::v-deep(.el-dialog) {
    .el-dialog__header {
      padding: 16px;
    }

    .el-dialog__body {
      padding: 16px;
      font-size: 14px;
      font-weight: 400;
      color: #333333;

      .content-wrapper {
        .operator {
          display: flex;
          align-items: center;
          justify-content: space-between;

          .btns {
            display: flex;
            align-items: center;
            color: #096dd9;

            .import,
            .clear {
              user-select: none;
              cursor: pointer;
              display: flex;
              align-items: center;

              .icon {
                font-size: 16px;
              }
            }

            .clear {
              margin-left: 8px;
            }
          }
        }

        .content-container {
          margin-top: 16px;

          .el-table {
            /*去掉鼠标移入改变当前行变背景色*/

            tbody tr:hover > td {
              background-color: transparent !important;
            }

            tbody tr > td {
              color: #333333;
            }

            // 表头样式
            .el-table__header-wrapper {
              table {
                height: 48px;
              }

              .header-row th {
                background: #eef0f3;
                border: none;

                &.webhook-param-value {
                  .cell {
                    padding-left: 0;
                  }
                }
              }
            }

            // 参数表格
            &.params-table {
              // 表主体样式
              .el-table__body-wrapper {
                .el-table__body {
                  .webhook-param-value {
                    padding: 0;

                    .cell {
                      position: relative;
                      height: 100%;
                      padding: 10px 0;

                      .el-form-item {
                        margin-bottom: 0;
                        position: relative;
                        top: 50%;
                        transform: translateY(-50%);

                        &.secret-item {
                          .el-form-item__content {
                            .el-input {
                              .el-input__inner[type='password'],
                              .el-input__inner[type='text'] {
                                padding: 0 22px 0 8px;
                              }

                              .el-input__suffix {
                                top: -4px;

                                .el-input__suffix-inner {
                                  &:hover {
                                    .el-icon-view.el-input__clear {
                                      &::before,
                                      &::after {
                                        color: #096dd9;
                                      }
                                    }
                                  }
                                }

                                i::before,
                                i::after {
                                  color: #6b7b8d;
                                  font-size: 16px;
                                }
                              }
                            }
                          }
                        }

                        .el-form-item__content {
                          line-height: inherit;

                          .el-textarea {
                            .el-textarea__inner {
                              box-sizing: border-box;
                              padding: 3px 8px;
                              line-height: 22px;
                              border-radius: 4px;
                            }

                            & + .el-form-item__error {
                              margin-top: 6px;
                            }
                          }

                          .el-input {
                            line-height: inherit;

                            .el-input__inner[type='number'],
                            .el-input__inner[type='password'],
                            .el-input__inner[type='text'] {
                              border-radius: 4px;
                              height: 30px;
                              padding: 0px 8px;
                              line-height: 22px;
                            }

                            & + .el-form-item__error {
                              margin-top: 6px;
                            }
                          }

                          .el-radio-group {
                            line-height: 16px;
                          }

                          .el-form-item__error {
                            position: static;
                            padding-top: 0;
                          }
                        }
                      }
                    }
                  }

                  .webhook-param-name {
                    .param-name {
                      display: flex;
                      align-items: center;

                      & > span {
                        max-width: 212px;
                        overflow: hidden;
                        white-space: nowrap;
                        text-overflow: ellipsis;
                      }

                      i {
                        width: 6px;
                        height: 10px;
                        background: url('@/components/theme/form/svgs/required.svg') no-repeat;
                        margin-right: 4px;
                      }
                    }
                  }
                }

                .el-table__body tr td {
                  height: 66px !important;
                  box-sizing: border-box;
                  border-bottom-color: #dee4eb;
                }
              }
            }

            // auth 表格
            &.auth-table {
              // 表主体样式
              .el-table__body-wrapper {
                .el-table__body tr td {
                  height: 48px !important;
                  box-sizing: border-box;
                  border-bottom-color: #dee4eb;
                  color: #5f708a;
                }
              }
            }
          }

          .auth-wrapper,
          .only-wrapper {
            .title {
              color: #082340;
              font-size: 16px;
              line-height: 22px;
              font-weight: 500;
              margin: 20px 0 8px;
            }
          }

          .only-wrapper {
            .only {
              background: #eef0f3;
              color: #5f708a;
              box-sizing: border-box;
              padding: 13px 16px;
            }
          }
        }
      }
    }
  }
}
</style>
