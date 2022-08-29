<template>
  <div class="custom-webhook-detail">
    <!-- 触发事件 -->
    <div class="trigger-event">
      <div class="trigger-title">触发事件</div>
      <jm-table class="trigger-table" :data="triggerEvent">
        <jm-table-column>
          <template #default="scope">
            <jm-text-viewer :threshold="0" :value="scope.row.name"/>
          </template>
        </jm-table-column>
        <jm-table-column width="200px" align="center">
          <template #default="scope">
            <span :style="{color:scope.row.succeed?'#10C2C2':'#FF1515'}">{{ scope.row.succeed ? '满足' : '不满足' }}</span>
          </template>
        </jm-table-column>
      </jm-table>
    </div>
    <!-- 触发规则 -->
    <div class="trigger-rule" v-if="triggerRules.length>0">
      <div class="trigger-title">触发规则</div>
      <jm-table class="trigger-table" :data="triggerRules">
        <jm-table-column>
          <template #default="scope">
            <jm-text-viewer :threshold="0" :value="scope.row.ruleStr"/>
          </template>
        </jm-table-column>
        <jm-table-column width="200px" align="center">
          <template #default="scope">
            <span :style="{color:scope.row.succeed?'#10C2C2':'#FF1515'}">{{ scope.row.succeed ? '满足' : '不满足' }}</span>
          </template>
        </jm-table-column>
      </jm-table>
      <!-- 规则大于等于两条显示提示 -->
      <span class="tips" v-if="triggerRules.length>1">{{ tips }}触发执行</span>
    </div>
    <!-- 参数列表 -->
    <div class="trigger-list">
      <div class="trigger-title">参数列表</div>
      <jm-table class="trigger-table" :data="webhookParamsDetail?.param">
        <jm-table-column label="参数唯一标识">
          <template #default="scope">
            <jm-text-viewer :threshold="0" :value="scope.row.ref"/>
          </template>
        </jm-table-column>
        <jm-table-column label="参数名称">
          <template #default="scope">
            <jm-text-viewer :threshold="0" :value="scope.row.name"/>
          </template>
        </jm-table-column>
        <jm-table-column label="参数类型" width="200px" prop="type">
        </jm-table-column>
        <jm-table-column label="参数值" prop="value">
          <template #default="scope">
            <div v-if="scope.row.hidden">
              <!-- 密钥类型切换 -->
              <div class="hide-container" v-if="scope.row.hidden">
                {{ scope.row.value }}
              </div>
              <div class="display-container" v-else>
                <template v-if="scope.row.value">
                  <div class="param-value"
                       :style="{maxWidth:maxWidthRecord[scope.row.value]?`${maxWidthRecord[scope.row.value]}px`: '100%'}">
                    <jm-text-viewer :threshold="0"
                                    v-if="!scope.row.hidden"
                                    :value="scope.row.value"
                                    @loaded="({contentMaxWidth})=>getTotalWidth(contentMaxWidth,scope.row.value)"
                                    class="value"
                    >
                    </jm-text-viewer>
                    <template v-else>
                      {{ scope.row.value }}
                    </template>
                  </div>
                </template>
              </div>
            </div>
            <template v-else>
              <param-value
                :value="scope.row.value"
                :type="scope.row.valueType"
              />
            </template>
          </template>
        </jm-table-column>
      </jm-table>
    </div>
  </div>
</template>

<script lang='ts'>
import { computed, defineComponent, PropType } from 'vue';
import { IWebhookParamVo, IWebhookRuleVo } from '@/api/dto/trigger';
import ParamValue from '@/views/common/param-value.vue';

export default defineComponent({
  props: {
    webhookParamsDetail: {
      type: Object as PropType<IWebhookParamVo>,
      required: true,
    },
  },
  components: {
    ParamValue,
  },
  setup(props) {
    const triggerEvent = computed(() => [{
      name: props.webhookParamsDetail.webhookEvent!.name,
      succeed: true,
    }]);
    const triggerRules = computed<IWebhookRuleVo[]>(() => props.webhookParamsDetail.webhookEvent!.ruleset);
    const tips = computed<string>(() => props.webhookParamsDetail.webhookEvent!.rulesetOperator);
    return {
      triggerEvent,
      triggerRules,
      tips,
    };
  },
});
</script>

<style scoped lang='less'>
.custom-webhook-detail {
  padding: 20px;

  .trigger-rule, .trigger-list {
    margin-top: 24px;
  }

  .trigger-rule {
    .tips {
      display: inline-block;
      margin-top: 10px;
      color: #7B8C9C;
      font-size: 12px;
      font-weight: 400;
    }
  }

  .trigger-event, .trigger-rule {
    .trigger-table {
      ::v-deep(.el-table__header-wrapper) {
        display: none;
      }
    }
  }
}
</style>
