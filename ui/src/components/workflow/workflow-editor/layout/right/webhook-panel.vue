<template>
  <div class="jm-workflow-editor-webhook-panel">
    <jm-form
      :model="form"
      :rules="form.getFormRules()"
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
              引用触发器参数参考<a href="https://docs.jianmu.dev/guide/vars.html#%E8%A7%A6%E5%8F%91%E5%99%A8%E5%8F%82%E6%95%B0"
                          target="_blank" style="color:#fff;text-decoration: underline;">参数章节</a>
            </template>
            <i class="jm-icon-button-help"></i>
          </jm-tooltip>
        </div>
        <div class="param-content" v-if="foldParamFlag">
          <webhook-param
            v-for="(param,index) in form.params"
            :key="index"
            v-model:name="param.name"
            v-model:exp="param.exp"
            v-model:type="param.type"
            v-model:required="param.required"
            v-model:default="param.default"
            :form-model-name="'params'"
            :index="index"
            :rules="nodeData.getFormRules()"
            @delete="deleteParam"
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
            认证规则
            <a href="https://docs.jianmu.dev/guide/webhook.html#%E5%A6%82%E4%BD%95%E5%AE%9A%E4%B9%89" target="_blank"><i
              class="jm-icon-button-help"></i></a>
          </div>
          <div class="rules-content">
            <jm-form-item prop="auth_token">
              <template #label>
                token
                <jm-tooltip content="Webhook请求携带的认证鉴权数据" placement="top">
                  <i class="jm-icon-button-help"></i>
                </jm-tooltip>
              </template>
              <jm-input v-model="form.auth.token" placeholder="请输入token"/>
            </jm-form-item>
            <jm-form-item prop="auth_value">
              <template #label>
                value
                <jm-tooltip content="用于校验token值，相同则验证成功，必须是密钥类型" placement="top">
                  <i class="jm-icon-button-help"></i>
                </jm-tooltip>
              </template>
              <secret-key-selector v-model="form.auth.value" placeholder="请输入value值"/>
            </jm-form-item>
          </div>
          <jm-form-item prop="only">
            <template #label>
              only
              <jm-tooltip placement="top">
                <template #content>
                  匹配规则，结果为 true 时触发流程，当前只可引用触发器参数详见<a href="https://docs.jianmu.dev/guide/expression.html"
                                                      target="_blank"
                                                      style="color:#fff;text-decoration: underline;">运算表达式</a>
                </template>
                <i class="jm-icon-button-help"></i>
              </jm-tooltip>
            </template>
            <div class="only-content">
              <jm-input type="textarea" placeholder="请输入匹配规则" v-model="form.only"/>
            </div>
          </jm-form-item>
        </div>
      </div>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { Webhook } from '../../model/data/node/webhook';
import WebhookParam from './form/webhook-param.vue';
import SecretKeySelector from './form/secret-key-selector.vue';

export default defineComponent({
  components: { WebhookParam, SecretKeySelector },
  props: {
    nodeData: {
      type: Object as PropType<Webhook>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const formRef = ref();
    const form = ref<Webhook>(props.nodeData);

    onMounted(() => emit('form-created', formRef.value));
    // 折叠
    const foldParamFlag = ref<boolean>(true);
    const foldSettingFlag = ref<boolean>(true);

    return {
      formRef,
      form,
      foldParamFlag,
      foldSettingFlag,
      //  删除
      deleteParam: (index: number) => {
        form.value.params.splice(index, 1);
      },
      addParam: () => {
        form.value.params.push({ name: '', exp: '', type: '', required: false, default: '' });
      },
      foldParam: () => {
        foldParamFlag.value = !foldParamFlag.value;
      },
      foldSetting: () => {
        foldSettingFlag.value = !foldSettingFlag.value;
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
  margin-bottom: 36px;

  ::v-deep(.el-form-item__error) {
    position: static;
  }

  // 页面全局设置，所有折叠按钮生效
  .jm-icon-button-right::before {
    font-size: 12px;
    color: #6B7B8D;
    transform: scale(0.8);
  }

  .jm-icon-button-help::before {
    font-size: 14px;
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
      margin: 10px 0;
    }

    .param-content {
      border-radius: 2px;
      border: 1px solid #E6EBF2;

      .add-param {
        padding: 10px 20px;
        font-size: 14px;
        color: #096DD9;
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
        .jm-icon-button-help {
          display: inline-block;
          cursor: pointer;
          width: 20px;
          height: 20px;

          &:hover {
            background: #EFF7FF;
            color: #086CD8;
          }
        }
      }

      .rules-content {
        padding: 20px;
        border-radius: 2px;
        border: 1px solid #E6EBF2;
        margin: 10px 0 20px;
      }

      .only {
        margin: 20px 0 10px 0;
      }
    }
  }
}
</style>