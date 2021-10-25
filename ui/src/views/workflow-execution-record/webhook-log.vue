<template>
  <div class="workflow-execution-record-webhook-log">
    <div class="basic-section">
      <div class="param-key">流程名称：
      </div>
      <jm-tooltip :content="workflowName" placement="bottom" effect="light">
        <div class="param-value">{{ workflowName }}</div>
      </jm-tooltip>
      <div class="param-key">节点名称：</div>
      <jm-tooltip :content="ebTargetId" placement="bottom" effect="light">
        <div class="param-value">{{ ebTargetId }}</div>
      </jm-tooltip>
      <div class="param-key">启动时间：</div>
      <jm-tooltip :content="startTime" placement="bottom" effect="light">
        <div class="param-value">{{ startTime }}</div>
      </jm-tooltip>
    </div>

    <div class="tab-section">
      <jm-tabs v-model="tabActiveName" @tab-click="handleTabClick">
        <jm-tab-pane name="log" lazy>
          <template #label>
            <div class="tab">日志</div>
          </template>
          <div class="tab-content">
            <div class="log">
              <jm-log-viewer id="webhook-log" :filename="`${ebTargetId}.txt`" :value="webhookLog"/>
            </div>
          </div>
        </jm-tab-pane>
        <jm-tab-pane name="params" lazy>
          <template #label>
            <div class="tab">业务参数</div>
          </template>
          <div class="tab-content">
            <div class="params" id="webhook-params">
              <jm-scrollbar>
                <div class="content">
                  <jm-table
                    :data="webhookParams"
                    border>
                    <jm-table-column
                      label="参数唯一标识"
                      align="center"
                      prop="name">
                    </jm-table-column>
                    <jm-table-column
                      label="参数类型"
                      align="center"
                      prop="type">
                    </jm-table-column>
                    <jm-table-column
                      label="参数值"
                      align="center"
                      prop="value">
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
import { defineComponent, getCurrentInstance, onMounted, ref } from 'vue';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { datetimeFormatter } from '@/utils/formatter';
import { fetchTargetEvent } from '@/api/view-no-auth';
import { adaptHeight, IAutoHeight } from '@/utils/auto-height';
import { IEventParameterVo } from '@/api/dto/event-bridge';

const autoHeights: {
  [key: string]: IAutoHeight;
} = {
  log: {
    elementId: 'webhook-log',
    offsetTop: 258,
  },
  params: {
    elementId: 'webhook-params',
    offsetTop: 226,
  },
};

export default defineComponent({
  props: {
    ebTargetId: {
      type: String,
      required: true,
    },
    triggerId: {
      type: String,
    },
    tabType: {
      type: String,
      required: true,
    },
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const tabActiveName = ref<string>(props.tabType);
    const webhookLog = ref<string>('');
    const webhookParams = ref<IEventParameterVo[]>([]);

    proxy.$nextTick(() => adaptHeight(autoHeights[tabActiveName.value]));

    onMounted(async () => {
      if (!props.triggerId) {
        // 尚未触发
        webhookLog.value = '尚未触发\n';
        return;
      }

      try {
        // 初始化Webhook
        const { payload, eventParameters } = await fetchTargetEvent(props.triggerId);
        webhookLog.value = 'Webhook:\n' +
          `payload: ${JSON.stringify(JSON.parse(payload), null, 2)}\n`;
        webhookParams.value = eventParameters;
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    });

    return {
      workflowName: state.recordDetail.record?.name,
      startTime: datetimeFormatter(state.recordDetail.record?.startTime),
      tabActiveName,
      webhookLog,
      webhookParams,
      handleTabClick: ({ props: { name } }: any) => {
        proxy.$nextTick(() => adaptHeight(autoHeights[name]));
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
  background-color: #FFFFFF;
  height: 100%;

  .basic-section {
    margin: 20px;
    padding: 16px 20px;
    display: flex;
    align-items: center;
    box-shadow: 0 0 8px 0 #9EB1C5;
    cursor: default;

    .param-key {
      color: #6B7B8D;
      line-height: 25px;

      &:nth-child(n + 2) {
        margin-left: 30px;
      }
    }

    .param-value {
      display: inline-block;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      max-width: 20%;
      color: #082340;
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
        background-color: #EEF0F3;
        color: #082340;
        border-radius: 6px 6px 0 0;
      }

      &.is-active {
        .tab {
          background-color: #082340;
          color: #FFFFFF;
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
    border: 1px solid #EEF0F7;

    .log {
      margin: 16px;
    }

    .params {
      background-color: #FFFFFF;
      border-radius: 4px;
      color: #082340;

      .content {
        padding: 16px;

        ::v-deep(.el-table) {
          th, td {
            color: #082340;
          }
        }
      }
    }
  }
}
</style>
