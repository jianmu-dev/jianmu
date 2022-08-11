<template>
  <div class="custom-webhook-events">
    <jm-checkbox v-model="isCheckedVal" :label="name" @change="changeChecked" class="check-event"/>
    <div v-if="eventInstanceVal && isCheckedVal">
      <Rule
        v-for="(rule,index) in eventInstanceVal.ruleset"
        :key="rule.key"
        v-model:paramRef="rule.paramRef"
        v-model:operator="rule.operator"
        v-model:matchingValue="rule.matchingValue"
        :available-params="availableParams"
        :index="index"
        @update:paramRef="val=>updateParamRef(val,index)"
        @update:operator="val=>updateOperator(val,index)"
        @update:matchingValue="val=>updateMatchingValue(val,index)"
        @delete="del"
      />
      <div class="add" @click="add">
        <i class="jm-icon-button-add"/>
        添加匹配规则
      </div>
      <jm-radio-group v-model="eventInstanceVal.rulesetOperator" @change="changeRulesetOperatorVal">
        <jm-radio v-for="{ref,name} in rulesetOperators" :key="ref" :label="ref">{{ name }}</jm-radio>
      </jm-radio-group>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import Rule from './custom-webhook-rule.vue';
import { getWebhookOperator, ICustomWebhookEventInstance } from '../../../model/data/node/custom-webhook';
import { IWebhookParam } from '../../../model/data/node/webhook';
import { v4 as uuidv4 } from 'uuid';
import { IWebhookEventOperatorVo } from '@/api/dto/custom-webhook';

export default defineComponent({
  components: { Rule },
  props: {
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
  },
  emits: ['update:eventInstance'],
  setup(props, { emit }) {
    const isCheckedVal = ref<boolean>(false);
    const eventInstanceVal = ref<ICustomWebhookEventInstance>(props.eventInstance);
    isCheckedVal.value = !!props.eventInstance;

    const rulesetOperators = ref<IWebhookEventOperatorVo[]>([]);
    onMounted(async () => {
      rulesetOperators.value = (await getWebhookOperator()).rulesetOperators;
    });
    return {
      isCheckedVal,
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
      changeChecked: (val: boolean) => {
        eventInstanceVal.value = val ? {
          ref: props.reference,
          ruleset: [],
          rulesetOperator: rulesetOperators.value[0].ref,
        } : undefined;
        // 初始化-勾选自动新增一条
        if (eventInstanceVal.value && eventInstanceVal.value.ruleset.length === 0) {
          eventInstanceVal.value.ruleset.push({ key: uuidv4(), paramRef: '', operator: 'INCLUDE', matchingValue: '' });
        }
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
.custom-webhook-events {
  color: #3F536E;
  font-size: 14px;
  border-bottom: 1px solid #E6EBF2;
  margin-bottom: 20px;
  padding-bottom: 20px;

  &.custom-webhook-events:last-child {
    border-bottom: none;
    margin-bottom: 0;
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
    margin-bottom: 20px;

    .jm-icon-button-add::before {
      font-weight: 700;
    }
  }
}
</style>
