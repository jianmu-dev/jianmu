<template>
  <div class="jm-workflow-editor-webhook-panel">
    <jm-form :model="form" ref="formRef" @submit.prevent label-position="top">
      <!-- 触发器参数 -->
      <div class="webhook-param">
        <div class="param-title">
          <span :class="{ fold: true, folded: foldParamFlag }" @click="foldParam">
            <i class="jm-icon-button-right"></i>
          </span>
          <span class="title-text">{{ $t('webhookPanel.triggerParam') }}</span>
          <jm-tooltip placement="top">
            <template #content>
              {{ $t('webhookPanel.triggerParamTip')
              }}<a
                href="https://v2.jianmu.dev/guide/vars.html#%E8%A7%A6%E5%8F%91%E5%99%A8%E5%8F%82%E6%95%B0"
                target="_blank"
                style="color: #fff; text-decoration: underline"
                >{{ $t('webhookPanel.paramSection') }}</a
              >
            </template>
            <i class="jm-icon-button-help"></i>
          </jm-tooltip>
        </div>
        <div class="param-content" v-if="foldParamFlag">
          <webhook-param
            v-for="(param, index) in form.params"
            :key="param.key"
            v-model:name="param.name"
            v-model:exp="param.exp"
            v-model:type="param.type"
            v-model:required="param.required"
            v-model:default="param.default"
            :form-model-name="'params'"
            :index="index"
            :rules="nodeData.getFormRules().params.fields[index].fields"
            @delete="deleteParam"
            @update:name="refreshEditorParams"
            @change-name="(newVal, oldVal) => changeName(index, oldVal, 'params')"
          />
          <div class="add-param" @click="addParam">
            <i class="jm-icon-button-add" />
            {{ $t('webhookPanel.addTriggerParam') }}
          </div>
        </div>
      </div>
      <!-- 高级设置 -->
      <div class="advanced-setting">
        <div class="param-title">
          <span :class="{ fold: true, folded: foldSettingFlag }" @click="foldSetting">
            <i class="jm-icon-button-right"></i>
          </span>
          <span class="title-text">{{ $t('webhookPanel.advancedSetting') }}</span>
        </div>
        <div class="setting-content" v-if="foldSettingFlag">
          <i class="line"></i>
          <div class="rules">
            <div class="rules-title">
              {{ $t('webhookPanel.authRule') }}
              <jm-tooltip placement="top">
                <template #content>
                  <div>{{ $t('webhookPanel.authRuleTip1') }}</div>
                  <div>{{ $t('webhookPanel.authRuleTip2') }}</div>
                </template>
                <i class="jm-icon-button-help"></i>
              </jm-tooltip>
            </div>
            <jm-switch v-model="authSwitch" @change="changeAuth" />
          </div>
          <div class="rules-content" v-if="authSwitch">
            <jm-form-item prop="auth.token" :rules="nodeData.getFormRules().auth.fields.token">
              <template #label>
                token
                <jm-tooltip :content="$t('webhookPanel.tokenTip')" placement="top">
                  <i class="jm-icon-button-help"></i>
                </jm-tooltip>
              </template>
              <expression-editor
                v-model="form.auth.token"
                :node-id="nodeId"
                :placeholder="$t('webhookPanel.tokenPlaceholder')"
                @editor-created="handleEditorCreated"
              />
            </jm-form-item>
            <jm-form-item prop="auth.value" :rules="nodeData.getFormRules().auth.fields.value">
              <template #label>
                value
                <jm-tooltip placement="top">
                  <template #content>
                    {{ $t('webhookPanel.valueTip1') }}<br />{{ $t('webhookPanel.valueTip2') }}</template
                  >
                  <i class="jm-icon-button-help"></i>
                </jm-tooltip>
              </template>
              <secret-key-selector v-model="form.auth.value" :placeholder="$t('webhookPanel.valuePlaceholder')" />
            </jm-form-item>
          </div>
          <jm-form-item class="only-container" prop="only" :rules="nodeData.getFormRules().only">
            <template #label>
              only
              <jm-tooltip placement="top">
                <template #content>
                  <div>{{ $t('webhookPanel.onlyRule') }}</div>
                  <div>
                    <span>{{ $t('webhookPanel.onlyTip') }}</span>
                    <a
                      href="https://v2.jianmu.dev/guide/expression.html"
                      target="_blank"
                      style="color: #fff; text-decoration: underline"
                      >{{ $t('webhookPanel.onlyLink') }}</a
                    >
                  </div>
                </template>
                <i class="jm-icon-button-help"></i>
              </jm-tooltip>
            </template>
            <div class="only-content">
              <expression-editor
                v-model="form.only"
                :placeholder="$t('webhookPanel.onlyPlaceholder')"
                :node-id="nodeId"
                @editor-created="handleEditorCreated"
              />
            </div>
          </jm-form-item>
        </div>
      </div>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, nextTick, onMounted, PropType, ref } from 'vue';
import { Webhook } from '../../model/data/node/webhook';
import WebhookParam from './form/webhook-param.vue';
import SecretKeySelector from './form/secret-key-selector.vue';
import ExpressionEditor from './form/expression-editor.vue';

import { v4 as uuidv4 } from 'uuid';
// eslint-disable-next-line no-redeclare
import { Node } from '@antv/x6';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';

export default defineComponent({
  components: { WebhookParam, SecretKeySelector, ExpressionEditor },
  props: {
    nodeData: {
      type: Object as PropType<Webhook>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const refreshParamsFns: ((params: ISelectableParam[]) => void)[] = [];
    const handleEditorCreated = (refreshParams: (params: ISelectableParam[]) => void) => {
      refreshParamsFns.push(refreshParams);
    };
    const formRef = ref();
    const form = ref<Webhook>(props.nodeData);
    const refreshEditorParams = async () => {
      await nextTick();
      const param = form.value.buildSelectableParam();
      refreshParamsFns.forEach(fn => fn(param ? [param] : []));
    };
    const nodeId = ref<string>('');
    const getNode = inject('getNode') as () => Node;
    nodeId.value = getNode().id;

    onMounted(() => emit('form-created', formRef.value));
    // 折叠
    const foldParamFlag = ref<boolean>(true);
    const foldSettingFlag = ref<boolean>(true);
    const authSwitch = ref<boolean>(!!props.nodeData.auth);

    return {
      refreshEditorParams,
      handleEditorCreated,
      formRef,
      form,
      foldParamFlag,
      foldSettingFlag,
      authSwitch,
      nodeId,
      //  删除
      deleteParam: (index: number) => {
        form.value.params.splice(index, 1);
        refreshEditorParams();
      },
      addParam: () => {
        form.value.params.push({ key: uuidv4(), name: '', exp: '', type: undefined, required: false });
        refreshEditorParams();
      },
      foldParam: () => {
        foldParamFlag.value = !foldParamFlag.value;
      },
      foldSetting: () => {
        foldSettingFlag.value = !foldSettingFlag.value;
      },
      changeAuth: (val: boolean) => {
        form.value.auth = val
          ? {
            token: '',
            value: '',
          }
          : undefined;
        if (val) {
          refreshEditorParams();
        }
      },
      changeName: (index: number, oldVal: string, formModelName: string) => {
        form.value.params.forEach(({ name }, idx) => {
          if (index === idx || name !== oldVal) {
            return;
          }
          console.log(index, idx);
          formRef.value.validateField(`${formModelName}.${idx}.name`);
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-webhook-panel {
  color: @label-color;
  font-size: 14px;
  margin: 20px 0;
  padding: 0 20px;

  // 页面全局设置，所有折叠按钮生效
  .jm-icon-button-right::before {
    font-size: 12px;
    color: #6b7b8d;
    transform: scale(0.8);
  }

  .jm-icon-button-help::before {
    font-size: 14px;
    margin: 0;
  }

  .jm-icon-button-help {
    display: inline-block;
    width: 20px;
    height: 20px;
  }

  .param-title {
    display: flex;
    align-items: center;
    color: @title-color;

    .title-text {
      margin: 0 6px 0 6px;
    }
  }

  .webhook-param {
    // 折叠
    .fold {
      cursor: pointer;
      font-size: 12px;
      transition: all 0.1s linear;

      &.folded {
        transform: rotate(90deg);
        transition: all 0.1s linear;
      }
    }

    .param-title {
      margin-bottom: 10px;
    }

    .param-content {
      border-radius: 2px;
      border: 1px solid #e6ebf2;

      .add-param {
        padding: 10px 20px;
        font-size: 14px;
        color: @primary-color;
        cursor: pointer;

        .jm-icon-button-add::before {
          font-weight: 700;
        }
      }
    }
  }

  .advanced-setting {
    margin-top: 20px;
    // 折叠
    .fold {
      cursor: pointer;
      font-size: 12px;
      transition: all 0.1s linear;

      &.folded {
        transform: rotate(90deg);
        transition: all 0.1s linear;
      }
    }

    .setting-content {
      .line {
        display: inline-block;
        height: 1px;
        width: 100%;
        background: #e6ebf2;
        margin: 20px 0;
      }

      .rules {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      .rules-content {
        padding: 20px 20px 10px;
        border-radius: 2px;
        border: 1px solid #e6ebf2;
        margin-top: 10px;
      }

      .only-container {
        margin: 20px 0 0;
      }

      .only-content {
        //::v-deep(.container) {
        //  height: 146px;
        //}
      }
    }
  }
}
</style>
