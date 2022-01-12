<template>
  <div class="workflow-execution-record-webhook-log">
    <div class="basic-section">
      <div class="param-key">流程名称：
      </div>
      <jm-text-viewer :value="workflowName" class="param-value" />
      <div class="param-key">节点名称：</div>
      <jm-text-viewer :value="nodeName" class="param-value node-name" />
      <div class="param-key">启动时间：</div>
      <jm-text-viewer :value="startTime" class="param-value" />
    </div>

    <div class="tab-section">
      <jm-tabs v-model="tabActiveName">
        <jm-tab-pane name="log" lazy>
          <template #label>
            <div class="tab">日志</div>
          </template>
          <div class="tab-content">
            <div class="log">
              <jm-log-viewer :filename="`${nodeName}.txt`" :value="webhookLog"/>
            </div>
          </div>
        </jm-tab-pane>
        <jm-tab-pane name="params" lazy>
          <template #label>
            <div class="tab">业务参数</div>
          </template>
          <div class="tab-content">
            <div class="params">
              <jm-scrollbar>
                <div class="content">
                  <jm-table
                    :data="webhookParams"
                    border>
                    <jm-table-column label="参数唯一标识">
                      <template #default="scope">
                        <jm-text-viewer :value="scope.row.name"  class="params-name"/>
                      </template>
                    </jm-table-column>
                    <jm-table-column
                      label="参数类型"
                      align="center"
                      prop="type">
                    </jm-table-column>
                    <jm-table-column
                      label="参数值"
                      align="center">
                      <template #default="scope">
                        <div class="copy-container">
                          <jm-text-viewer :value="scope.row.value" class="webhook-param-value"/>
                          <div class="copy-btn" @click="copy(scope.row.value)" v-if="scope.row.valueType !== ParamTypeEnum.SECRET"></div>
                        </div>
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
import { TriggerTypeEnum,ParamTypeEnum } from '@/api/dto/enumeration';
import useClipboard from 'vue-clipboard3';

export default defineComponent({
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
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const tabActiveName = ref<string>(props.tabType);
    const webhookLog = ref<string>('');
    const webhookParams = ref<IEventParameterVo[]>([]);
    const { toClipboard } = useClipboard();

    onMounted(async () => {
      if (!props.triggerId) {
        // 尚未触发
        webhookLog.value = '尚未触发\n';
        return;
      }

      try {
        // 初始化Webhook
        const { payload, parameters } = await fetchTriggerEvent(props.triggerId);
        webhookLog.value = 'Webhook:\n' +
          `payload: ${JSON.stringify(JSON.parse(payload), null, 2)}\n`;
        webhookParams.value = parameters;
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    });
    // 一键复制
    const copy = async (value:string) => {
      if (!value) {
        return;
      }
      try {
        await toClipboard(value);
        proxy.$success('复制成功');
      } catch (err) {
        proxy.$error('复制失败，请手动复制');
        console.error(err);
      }
    };
    return {
      workflowName: state.recordDetail.record?.name,
      startTime: datetimeFormatter(state.recordDetail.record?.startTime),
      tabActiveName,
      webhookLog,
      webhookParams,
      copy,
      ParamTypeEnum
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
      width: 20%;
      color: #082340;
    }
    .node-name{
      max-width:10%;
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
      height: calc(100vh - 258px)
    }

    .params {
      background-color: #FFFFFF;
      border-radius: 4px;
      color: #082340;
      height: calc(100vh - 226px);

      .content {
        padding: 16px;
        .params-name{
          width:80%;
        }
        ::v-deep(.el-table) {
          th, td {
            color: #082340;
          }
          tr{
            td:first-child,
            td:last-child{
              text-align:left;
              padding-left:20px;
            }
            td:first-child{
              .cell{
                width:100%!important;
              }
            }
          }
          .copy-container{
            display: flex;
            align-items: center;
            // 表格参数
            .webhook-param-value {
              width: 88%;
              position: relative;
            }
            &:hover{
              .copy-btn{
                width:16px;
                height:16px;
                background:url('@/assets/svgs/btn/copy.svg') no-repeat;
                background-size:100%;
                cursor: pointer;
                position:absolute;
                top:14px;
                right:10px;
                opacity: 0.5;
                &:hover{
                  opacity: 1;
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
