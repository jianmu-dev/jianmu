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
        <jm-select v-model="paramRefVal" placeholder="请选择参数" @change="changeParamRef">
          <jm-option
            v-for="item in availableParams"
            :key="item.ref"
            :label="item.name"
            :value="item.ref"
          />
        </jm-select>
      </jm-form-item>
      <div class="operator-container">
        <div class="switch-dropdown">
          <jm-dropdown trigger="click" @command="changeOperator" v-if="switchIconFlag">
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
          <span class="operator-text" v-else>{{ operatorText }}</span>
        </div>
        <div class="icon-container" v-if="paramType === ParamTypeEnum.BOOL">
          <jm-tooltip placement="top" :content="switchIconFlag?'切换至单选模式':'切换至表达式模式'" v-if="tooltipVisible">
            <i class="jm-icon-workflow-select-mode" v-if="switchIconFlag" @click="switchMode(false)"/>
            <i class="jm-icon-workflow-edit-mode" v-else @click="switchMode(true)"/>
          </jm-tooltip>
        </div>
      </div>
      <jm-form-item :prop="`${modelName}.${index}.matchingValue`" :rules="rules.matchingValue" v-if="reloadPlaceholder">
        <expression-editor
          v-if="switchIconFlag"
          v-model="matchingValueVal"
          :placeholder="inputPlaceholder"
          :type="ExpressionTypeEnum.WEBHOOK_PARAM"
          :param-type="paramType"
          :node-id="nodeId"
          @change="changeMatchingValue"
          @focus="switchFlag=true"
          @blur="switchFlag=false"
        />
        <jm-radio-group v-model="matchingValueVal" @change="changeMatchingValue" v-else>
          <jm-radio label="true" @change="changeMatchingValue">true</jm-radio>
          <jm-radio label="false" @change="changeMatchingValue">false</jm-radio>
        </jm-radio-group>
      </jm-form-item>
    </div>
  </div>
</template>
<script lang="ts">
import { computed, defineComponent, inject, nextTick, onMounted, PropType, ref } from 'vue';
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
      type: [String, Number, Boolean],
      default: '',
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
    const reloadPlaceholder = ref<boolean>(true);
    const switchIconFlag = ref<boolean>(true);
    const tooltipVisible = ref<boolean>(true);
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
    const matchingValueVal = ref<ParamValueType>(props.matchingValue);
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
      if (paramType.value === ParamTypeEnum.BOOL) {
        switchIconFlag.value = !(matchingValueVal.value === 'true' || matchingValueVal.value === 'false');
      }
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
      reloadPlaceholder,
      switchIconFlag,
      tooltipVisible,
      del: () => {
        emit('delete', props.index);
      },
      changeParamRef: async () => {
        switch (paramType.value) {
          case ParamTypeEnum.STRING:
            matchingValueVal.value = '""';
            switchIconFlag.value = true;
            break;
          case ParamTypeEnum.BOOL:
            matchingValueVal.value = (matchingValueVal.value === '""' || matchingValueVal.value === '') ? 'true' : matchingValueVal.value;
            switchIconFlag.value = false;
            break;
          case ParamTypeEnum.NUMBER:
            matchingValueVal.value = '';
            switchIconFlag.value = true;
            break;
        }
        emit('update:matchingValue', matchingValueVal.value);
        // changeParamRef时修改operator
        emit('update:operator', operatorOptions.value.find(({ name }) => name === operatorText.value)!.ref);
        emit('update:paramRef', paramRefVal.value);
        // 保证paramRef切换时值placeholder能更新
        reloadPlaceholder.value = false;
        await nextTick();
        reloadPlaceholder.value = true;
      },
      changeMatchingValue: () => {
        emit('update:matchingValue', matchingValueVal.value);
      },
      changeOperator,
      switchMode: async (val: boolean) => {
        // 切换
        switchIconFlag.value = val;
        // 更改operator状态
        if (!val) {
          // 单选 else分支表达式输入框
          operatorVal.value = operatorOptions.value[0].ref;
          // matchingValueVal.value = 'true';
          matchingValueVal.value = (matchingValueVal.value === 'true' || matchingValueVal.value === 'false') ? matchingValueVal.value : 'true';
          emit('update:operator', operatorVal.value);
        }
        emit('update:matchingValue', matchingValueVal.value);

        // 优化tooltip位移问题
        tooltipVisible.value = false;
        // 切换时隐藏表单校验
        reloadPlaceholder.value = false;
        await nextTick();
        tooltipVisible.value = true;
        reloadPlaceholder.value = true;
      },
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
    padding: 20px 20px 0;
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
        width: 20px;
        height: 20px;

        &::before {
          transform: rotate(90deg) scale(.9);
          font-size: 12px;
          position: relative;
          top: 0;
          left: -3px;
        }
      }
    }

    ::v-deep(.el-form-item) {
      margin-bottom: 20px;
    }

    .operator-container {
      display: flex;

      .icon-container {
        // 输入
        .jm-icon-workflow-select-mode,
        .jm-icon-workflow-edit-mode {
          width: 20px;
          height: 20px;
          color: #6B7B8D;
          margin-left: 10px;

          &:hover {
            cursor: pointer;
            color: #096DD9;
          }
        }

        .jm-icon-workflow-select-mode {
          margin-left: 6px;
        }
      }
    }

    ::v-deep(.el-radio-group) {
      margin-top: 14px;

      .el-radio {
        margin-right: 30px;
      }
    }
  }
}
</style>