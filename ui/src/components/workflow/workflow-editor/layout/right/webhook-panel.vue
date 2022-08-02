<template>
  <div class="jm-workflow-editor-webhook-panel">
    <jm-form
      :model="form"
      ref="formRef"
      @submit.prevent
      label-position="top"
    >
      <!-- 触发器参数 -->
      <div class="webhook-param">
        <div class="param-title">
            <span :class="{fold:true,folded:foldParamFlag}" @click="foldParam">
              <i class="jm-icon-button-right"></i>
            </span>
          <span class="title-text">触发器参数</span>
          <jm-tooltip placement="top">
            <template #content>
              引用触发器参数参考<a href="https://v2.jianmu.dev/guide/vars.html#%E8%A7%A6%E5%8F%91%E5%99%A8%E5%8F%82%E6%95%B0"
                          target="_blank" style="color:#fff;text-decoration: underline;">参数章节</a>
            </template>
            <i class="jm-icon-button-help"></i>
          </jm-tooltip>
        </div>
        <div class="param-content" v-if="foldParamFlag">
          <webhook-param
            v-for="(param,index) in form.params"
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
          />
          <div class="add-param" @click="addParam">
            <i class="jm-icon-button-add"/>
            添加触发器参数
          </div>
        </div>
      </div>
      <!-- 高级设置 -->
      <div class="advanced-setting">
        <div class="param-title">
            <span :class="{fold:true,folded:foldSettingFlag}" @click="foldSetting">
              <i class="jm-icon-button-right"></i>
            </span>
          <span class="title-text">高级设置</span>
        </div>
        <div class="setting-content" v-if="foldSettingFlag">
          <i class="line"></i>
          <div class="rules">
            <div class="rules-title">
              认证规则
              <jm-tooltip placement="top">
                <template #content>
                  <div>若关闭认证规则，任何人皆可通过</div>
                  <div>webhook触发此流程</div>
                </template>
                <i class="jm-icon-button-help"></i>
              </jm-tooltip>
            </div>
            <jm-switch v-model="authSwitch" @change="changeAuth"/>
          </div>
          <div class="rules-content" v-if="authSwitch">
            <jm-form-item prop="auth.token" :rules="nodeData.getFormRules().auth.fields.token">
              <template #label>
                token
                <jm-tooltip content="Webhook请求携带的认证鉴权数据" placement="top">
                  <i class="jm-icon-button-help"></i>
                </jm-tooltip>
              </template>
              <expression-editor
                v-model="form.auth.token"
                :node-id="nodeId"
                placeholder="请输入token值"
                @editor-created="handleEditorCreated"/>
            </jm-form-item>
            <jm-form-item prop="auth.value" :rules="nodeData.getFormRules().auth.fields.value">
              <template #label>
                value
                <jm-tooltip placement="top">
                  <template #content>
                    用于校验token值，相同则验证成<br/>功，必须是密钥类型
                  </template>
                  <i class="jm-icon-button-help"></i>
                </jm-tooltip>
              </template>
              <secret-key-selector v-model="form.auth.value" :placeholder="'请选择value值'"/>
            </jm-form-item>
          </div>
          <jm-form-item class="only-container" prop="only" :rules="nodeData.getFormRules().only">
            <template #label>
              only
              <jm-tooltip placement="top">
                <template #content>
                  <div>匹配规则，结果为 true 时触发流程，当</div>
                  <div>
                    <span>前只可引用触发器参数。详见</span>
                    <a href="https://v2.jianmu.dev/guide/expression.html"
                       target="_blank"
                       style="color:#fff;text-decoration: underline;">运算表达式</a>
                  </div>
                </template>
                <i class="jm-icon-button-help"></i>
              </jm-tooltip>
            </template>
            <div class="only-content">
              <expression-editor
                v-model="form.only"
                placeholder="请输入匹配规则"
                :node-id="nodeId"
                @editor-created="handleEditorCreated"/>
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
        form.value.auth = val ? {
          token: '',
          value: '',
        } : undefined;
        if (val) {
          refreshEditorParams();
        }
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
    color: #6B7B8D;
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
      transition: all .1s linear;

      &.folded {
        transform: rotate(90deg);
        transition: all .1s linear;
      }
    }

    .param-title {
      margin-bottom: 10px;
    }

    .param-content {
      border-radius: 2px;
      border: 1px solid #E6EBF2;

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
      transition: all .1s linear;

      &.folded {
        transform: rotate(90deg);
        transition: all .1s linear;
      }
    }

    .setting-content {
      .line {
        display: inline-block;
        height: 1px;
        width: 100%;
        background: #E6EBF2;
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
        border: 1px solid #E6EBF2;
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
