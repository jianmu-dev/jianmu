<template>
  <div class="custom-webhook-rule">
    <div class="tool-label">
      <div class="label">
        匹配规则{{ index + 1 }}
      </div>
      <i class="jm-icon-button-delete" @click="del"></i>
    </div>
    <div :class="['rules-container',switchFlag?'switch-bgc':'']">
      <jm-form-item :prop="`${modelName}.${index}.paramRef`" :rules="rules.paramRef">
        <jm-select v-model="paramRefVal" placeholder="请选择参数类型" @change="changeParamRef">
          <jm-option
            v-for="item in availableParams"
            :key="item.ref"
            :label="item.name"
            :value="item.ref"
          />
        </jm-select>
      </jm-form-item>
      <jm-dropdown trigger="click" @command="changeOperator">
        <span class="el-dropdown-link">
          {{ operatorText }}
          <i class="jm-icon-button-right"/>
        </span>
        <template #dropdown>
          <jm-dropdown-menu>
            <jm-dropdown-item v-for="item in operatorOptions" :key="item.ref" :command="item.ref">
              {{ item.name }}
            </jm-dropdown-item>
          </jm-dropdown-menu>
        </template>
      </jm-dropdown>
      <jm-form-item :prop="`${modelName}.${index}.matchingValue`" :rules="rules.matchingValue">
        <expression-editor
          v-model="matchingValueVal"
          :placeholder="inputPlaceholder"
          :type="ExpressionTypeEnum.WEBHOOK_PARAM"
          :param-type="paramType"
          :node-id="nodeId"
          @change="changeMatchingValue"
          @focus="switchFlag=true"
          @blur="switchFlag=false"
        />
      </jm-form-item>
    </div>
  </div>
</template>
<script lang="ts">
import { computed, defineComponent, inject, onMounted, PropType, ref } from 'vue';
import { Node } from '@antv/x6';
import { ExpressionTypeEnum, ParamTypeEnum } from '../../../model/data/enumeration';
import ExpressionEditor from '../form/expression-editor.vue';
import { IWebhookParam } from '../../../model/data/node/webhook';
import { IWebhookEventOperatorVo, IWebhookParamOperatorVo } from '@/api/dto/custom-webhook';
import { getWebhookOperator } from '../../../model/data/node/custom-webhook';
import { CustomRule, ParamValueType } from '../../../model/data/common';

export default defineComponent({
  components: { ExpressionEditor },
  props: {
    paramRef: {
      type: String,
      default: '',
    },
    operator: {
      type: String,
      default: '',
    },
    matchingValue: {
      type: Object as PropType<ParamValueType>,
    },
    availableParams: {
      type: Array as PropType<IWebhookParam[]>,
      required: true,
    },
    index: {
      type: Number,
      required: true,
    },
    rules: {
      type: Object as PropType<Record<string, CustomRule>>,
      required: true,
    },
    modelName: {
      type: String,
      required: true,
    },
  },
  emits: ['update:paramRef', 'update:operator', 'update:matchingValue', 'delete'],
  setup(props, { emit }) {
    const paramOperators = ref<IWebhookParamOperatorVo[]>([]);
    // 参数唯一标识
    const paramRefVal = ref<String>(props.paramRef);
    // 类型-值
    const operatorVal = ref<string>(props.operator);
    const paramType = computed<ParamTypeEnum>(() =>
      paramRefVal.value ? props.availableParams.find(({ ref }) => ref === paramRefVal.value)!.type : ParamTypeEnum.STRING);
    // 匹配规则下拉
    const operatorOptions = computed<IWebhookEventOperatorVo[]>(() =>
      paramOperators.value.length === 0 ? [] : paramOperators.value.find(({ type }) => type === paramType.value)!.operators);
    // 类型-文案
    const operatorText = computed<string>(() => {
      if (operatorVal.value) {
        const operatorOption = operatorOptions.value.find(({ ref }) => ref === operatorVal.value);
        return operatorOption ? operatorOption.name : (operatorOptions.value ? { ...operatorOptions.value[0] }.name : '');
      }
      return operatorOptions.value.length === 0 ? '' : operatorOptions.value[0].name;
    });
    // 输入框动态placeholder
    const inputPlaceholder = computed<string>(() =>
      paramRefVal.value ? `请输入${props.availableParams.find(({ ref }) => ref === paramRefVal.value)!.name}` : '');
    // 值
    const matchingValueVal = ref<ParamValueType>(props.matchingValue || '');
    if (!matchingValueVal.value && paramType.value === ParamTypeEnum.STRING) {
      matchingValueVal.value = '""';
      emit('update:matchingValue', matchingValueVal.value);
    }
    const switchFlag = ref<boolean>(false);

    const getNode = inject('getNode') as () => Node;
    const nodeId = getNode().id;

    const changeOperator = (val: string) => {
      operatorVal.value = val;
      emit('update:operator', val);
    };
    onMounted(async () => {
      paramOperators.value = (await getWebhookOperator()).paramOperators;
      if (operatorVal.value) {
        return;
      }
      changeOperator(paramOperators.value.find(({ type }) => type === ParamTypeEnum.STRING)!.operators[0].ref);
    });
    return {
      ExpressionTypeEnum,
      ParamTypeEnum,
      operatorOptions,
      inputPlaceholder,
      paramRefVal,
      operatorText,
      matchingValueVal,
      switchFlag,
      paramType,
      nodeId,
      del: () => {
        emit('delete', props.index);
      },
      changeParamRef: () => {
        emit('update:paramRef', paramRefVal.value);
      },
      changeMatchingValue: () => {
        emit('update:matchingValue', matchingValueVal.value);
      },
      changeOperator,
    };
  },
});
</script>

<style scoped lang="less">
.custom-webhook-rule {
  font-size: 14px;
  color: #3F536E;
  margin-bottom: 20px;

  &.custom-webhook-rule:first-child {
    margin-top: 20px;
  }

  .tool-label {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    .label {
      font-size: 14px;
    }

    // 删除
    .jm-icon-button-delete {
      display: inline-block;
      width: 20px;
      height: 20px;
      cursor: pointer;

      &:hover {
        color: #086CD8;
        background: #EFF7FF;
      }
    }
  }

  .rules-container {
    padding: 20px;
    border-radius: 2px;
    border: 1px solid #E6EBF2;

    &:hover {
      background: #FAFAFA;
    }

    &.switch-bgc {
      background: #EFF7FF;

      &:hover {
        background: #EFF7FF;
      }
    }

    ::v-deep(.el-select) {
      width: 100%;
    }

    ::v-deep(.el-dropdown) {
      margin-bottom: 10px;
    }

    ::v-deep(.el-dropdown-link) {
      color: #082340;

      .el-dropdown-menu {
        color: #082340;
      }

      .jm-icon-button-right {
        &::before {
          transform: rotate(90deg) scale(.9);
          font-size: 12px;
          position: relative;
          top: 0;
          left: -3px;
        }
      }
    }

    ::v-deep(.el-form-item):first-child {
      margin-bottom: 20px;
    }

    ::v-deep(.el-form-item):last-child {
      margin-bottom: 0;
    }
  }
}
</style>