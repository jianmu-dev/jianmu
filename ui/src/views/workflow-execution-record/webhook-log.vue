<template>
  <div class="workflow-execution-record-webhook-log">
    <div class="basic-section">
      <div class="param-key">{{ t('webhookLog.processName') }}</div>
      <jm-text-viewer :value="workflowName" :tip-append-to-body="false" class="param-value" />
      <div class="param-key">{{ t('webhookLog.nodeName') }}</div>
      <jm-text-viewer :value="nodeName" :tip-append-to-body="false" class="param-value node-name" />
      <div class="param-key">{{ t('webhookLog.startTime') }}</div>
      <jm-text-viewer :value="startTime" :tip-append-to-body="false" class="param-value" />
    </div>

    <div class="tab-section">
      <jm-tabs v-model="tabActiveName">
        <jm-tab-pane name="log" lazy>
          <template #label>
            <div class="tab">{{ t('webhookLog.log') }}</div>
          </template>
          <div class="tab-content">
            <div class="log">
              <jm-log-viewer v-if="webhookLog" :filename="`${nodeName}.txt`" :value="webhookLog" />
            </div>
          </div>
        </jm-tab-pane>
        <jm-tab-pane name="params" lazy>
          <template #label>
            <div class="tab">{{ t('webhookLog.businessParams') }}</div>
          </template>
          <div class="tab-content">
            <div class="params">
              <jm-scrollbar>
                <div class="content">
                  <jm-table :data="webhookParams" border>
                    <jm-table-column :label="t('webhookLog.paramKey')" align="center">
                      <template #default="scope">
                        <jm-text-viewer :value="scope.row.name" :tip-append-to-body="false" class="params-name" />
                      </template>
                    </jm-table-column>
                    <jm-table-column :label="t('webhookLog.paramType')" align="center" prop="type">
                      <template #default="scope">
                        <div class="text-viewer">
                          <jm-text-viewer :value="scope.row.type" :tip-append-to-body="false" class="params-name" />
                        </div>
                      </template>
                    </jm-table-column>
                    <jm-table-column :label="t('webhookLog.paramValue')" align="center">
                      <template #default="scope">
                        <param-value :value="scope.row.value" :tip-append-to-body="false" :type="scope.row.valueType" />
                      </template>
                    </jm-table-column>
                  </jm-table>
                </div>
              </jm-scrollbar>
            </div>
          </div>
        </jm-tab-pane>
      </jm-tabs>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onMounted, PropType, ref } from 'vue';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { datetimeFormatter } from '@/utils/formatter';
import { fetchTriggerEvent } from '@/api/view-no-auth';
import { IEventParameterVo } from '@/api/dto/trigger';
import { ParamTypeEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import JmTextViewer from '@/components/text-viewer/index.vue';
import ParamValue from '@/views/common/param-value.vue';
import { useLocale } from '@/utils/i18n';

export default defineComponent({
  components: { JmTextViewer, ParamValue },
  props: {
    nodeName: {
      type: String,
      required: true,
    },
    triggerId: String,
    triggerType: String as PropType<TriggerTypeEnum>,
    tabType: {
      type: String,
      required: true,
    },
  },
  setup(props: any) {
    const { t } = useLocale();
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const tabActiveName = ref<string>(props.tabType);
    const webhookLog = ref<string>('');
    const webhookParams = ref<IEventParameterVo[]>([]);
    const maxWidthRecord = ref<Record<string, number>>({});

    onMounted(async () => {
      if (!props.triggerId) {
        // 尚未触发
        webhookLog.value = `${t('webhookLog.notTriggered')}\n`;
        return;
      }

      try {
        // 初始化Webhook
        const { payload, parameters } = await fetchTriggerEvent(props.triggerId);
        webhookLog.value = payload
          ? 'Webhook:\n' + `payload: ${JSON.stringify(JSON.parse(payload), null, 2)}\n`
          : props.triggerType === TriggerTypeEnum.MANUAL
            ? t('webhookLog.manualTriggerLog')
            : '';
        webhookParams.value = parameters.sort((p1, p2) => p1.name.localeCompare(p2.name));
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    });
    return {
      t,
      workflowName: state.recordDetail.record?.name,
      startTime: datetimeFormatter(state.recordDetail.record?.startTime),
      tabActiveName,
      webhookLog,
      webhookParams,
      ParamTypeEnum,
      maxWidthRecord,
      getTotalWidth(width: number, ref: string) {
        maxWidthRecord.value[ref] = width;
      },
    };
  },
});
</script>

<style scoped lang="less">
.workflow-execution-record-webhook-log {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;
  background-color: #ffffff;
  height: 100%;

  .basic-section {
    margin: 20px;
    padding: 16px 20px;
    display: flex;
    align-items: center;
    box-shadow: 0 0 8px 0 #9eb1c5;
    cursor: default;

    .param-key {
      color: #6b7b8d;
      line-height: 25px;

      &:nth-child(n + 2) {
        margin-left: 30px;
      }
    }

    .param-value {
      display: inline-block;
      width: 20%;
      color: #082340;
    }

    .node-name {
      max-width: 10%;
    }
  }
}

.tab-section {
  margin: 0 20px;

  ::v-deep(.el-tabs) {
    .el-tabs__active-bar {
      display: none;
    }

    .el-tabs__item {
      padding: 0;

      .tab {
        width: 120px;
        height: 40px;
        text-align: center;
        background-color: #eef0f3;
        color: #082340;
        border-radius: 6px 6px 0 0;
      }

      &.is-active {
        .tab {
          background-color: #082340;
          color: #ffffff;
        }
      }
    }

    .el-tabs__item + .el-tabs__item {
      padding-left: 4px;
    }

    .el-tabs__nav-wrap {
      box-shadow: inherit;

      .el-tabs__nav-scroll {
        line-height: inherit;
      }
    }
  }

  .tab-content {
    border: 1px solid #eef0f7;

    .log {
      margin: 16px;
      height: calc(100vh - 258px);
    }

    .params {
      background-color: #ffffff;
      border-radius: 4px;
      color: #082340;
      height: calc(100vh - 226px);

      .content {
        padding: 16px;

        ::v-deep(.text-viewer) {
          .params-name {
            width: 100%;

            .content {
              .text-line {
                text-align: center;

                &::after {
                  display: none;
                }
              }
            }
          }
        }

        .params-name {
          width: 80%;
        }

        ::v-deep(.el-table) {
          overflow: visible;

          .el-table__body-wrapper {
            overflow: visible;

            th,
            td {
              color: #082340;
            }

            tr {
              td:first-child,
              td:last-child {
                text-align: left;
                padding-left: 20px;
              }

              td:first-child {
                .cell {
                  width: 100% !important;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
