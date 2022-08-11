<template>
  <div class="custom-webhook-event">
    <jm-radio :label="reference">{{ name }}</jm-radio>
    <div v-if="eventInstanceVal">
      <Rule
        v-for="(rule,idx) in eventInstanceVal.ruleset"
        :key="rule.key"
        v-model:paramRef="rule.paramRef"
        v-model:operator="rule.operator"
        v-model:matchingValue="rule.matchingValue"
        :available-params="availableParams"
        :index="idx"
        :rules="rules.ruleset.fields[idx].fields"
        :model-name="`${formModelName}.${index}.ruleset`"
        @update:paramRef="val=>updateParamRef(val,idx)"
        @update:operator="val=>updateOperator(val,idx)"
        @update:matchingValue="val=>updateMatchingValue(val,idx)"
        @delete="del"
      />
      <div class="add" @click="add">
        <i class="jm-icon-button-add"/>
        添加匹配规则
      </div>
      <jm-form-item :prop="`${formModelName}.${index}.rulesetOperator`" :rules="rules.rulesetOperator">
        <jm-radio-group v-model="eventInstanceVal.rulesetOperator" @change="changeRulesetOperatorVal">
          <jm-radio v-for="{ref,name} in rulesetOperators" :key="ref" :label="ref">{{ name }}</jm-radio>
        </jm-radio-group>
      </jm-form-item>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, onUpdated, PropType, ref } from 'vue';
import Rule from './custom-webhook-rule.vue';
import { getWebhookOperator, ICustomWebhookEventInstance } from '../../../model/data/node/custom-webhook';
import { IWebhookParam } from '../../../model/data/node/webhook';
import { v4 as uuidv4 } from 'uuid';
import { IWebhookEventOperatorVo } from '@/api/dto/custom-webhook';
import { CustomRule } from '../../../model/data/common';

export default defineComponent({
  components: { Rule },
  props: {
    selectedReference: {
      type: String,
      required: true,
    },
    name: {
      type: String,
      required: true,
    },
    reference: {
      type: String,
      required: true,
    },
    availableParams: {
      type: Array as PropType<IWebhookParam[]>,
      required: true,
    },
    eventInstance: {
      type: Object as PropType<ICustomWebhookEventInstance>,
    },
    index: {
      type: Number,
      required: true,
    },
    rules: {
      type: Object as PropType<Record<string, CustomRule>>,
    },
    formModelName: {
      type: String,
      required: true,
    },
  },
  emits: ['update:eventInstance', 'check-event'],
  setup(props, { emit }) {
    const eventInstanceVal = ref<ICustomWebhookEventInstance>(props.eventInstance);
    const rulesetOperators = ref<IWebhookEventOperatorVo[]>([]);
    const selectedReferenceVal = ref<string>(props.selectedReference);
    const isChecked = ref<boolean>(false);

    // 初始化时判断是否勾选
    if (eventInstanceVal.value && eventInstanceVal.value.ref === props.reference) {
      emit('check-event', props.reference);
    }

    const changeReference = async (val: boolean) => {
      eventInstanceVal.value = val ? props.eventInstance || {
        ref: props.reference,
        ruleset: [],
        rulesetOperator: (await getWebhookOperator()).rulesetOperators[0].ref,
      } : undefined;
      // 初始化-勾选自动新增一条
      if (eventInstanceVal.value && eventInstanceVal.value.ruleset.length === 0) {
        eventInstanceVal.value.ruleset.push({ key: uuidv4(), paramRef: '', operator: 'INCLUDE', matchingValue: '' });
      }
      emit('update:eventInstance', eventInstanceVal.value);
    };

    onUpdated(async () => {
      if (selectedReferenceVal.value === props.selectedReference) {
        return;
      }
      selectedReferenceVal.value = props.selectedReference;
      await changeReference(selectedReferenceVal.value === props.reference);
    });
    onMounted(async () => {
      rulesetOperators.value = (await getWebhookOperator()).rulesetOperators;
    });
    return {
      isChecked,
      eventInstanceVal,
      rulesetOperators,
      updateParamRef: (val: string, index: number) => {
        eventInstanceVal.value.ruleset[index].paramRef = val;
        emit('update:eventInstance', eventInstanceVal.value);
      },
      updateOperator: (val: string, index: number) => {
        eventInstanceVal.value.ruleset[index].operator = val;
        emit('update:eventInstance', eventInstanceVal.value);
      },
      updateMatchingValue: (val: string, index: number) => {
        eventInstanceVal.value.ruleset[index].matchingValue = val;
        emit('update:eventInstance', eventInstanceVal.value);
      },
      del: (index: number) => {
        eventInstanceVal.value!.ruleset.splice(index, 1);
        emit('update:eventInstance', eventInstanceVal.value);
      },
      add: () => {
        eventInstanceVal.value.ruleset.push({ key: uuidv4(), paramRef: '', operator: 'INCLUDE', matchingValue: '' });
        emit('update:eventInstance', eventInstanceVal.value);
      },
      changeRulesetOperatorVal: () => {
        emit('update:eventInstance', eventInstanceVal.value);
      },
    };
  },
});
</script>

<style scoped lang="less">
.custom-webhook-event {
  color: #3F536E;
  font-size: 14px;
  border-bottom: 1px solid #E6EBF2;
  margin-bottom: 20px;
  padding-bottom: 20px;
  box-sizing: border-box;

  &.custom-webhook-event:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }

  ::v-deep(.el-radio-group) {
    margin-left: 0;
  }

  .check-event {
    display: flex;
    // 文字换行
    word-break: break-all;
    white-space: pre-wrap;

    ::v-deep(.el-checkbox__inner) {
      position: relative;
      top: 2px;
    }
  }

  .add {
    font-size: 14px;
    color: #096DD9;
    cursor: pointer;
    padding: 14px 20px;
    border: 1px solid #E6EBF2;
    box-sizing: border-box;
    margin: 20px 0;

    .jm-icon-button-add::before {
      font-weight: 700;
    }
  }
}
</style>
