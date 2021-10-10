<template>
  <div class="workflow-execution-record-task-log">
    <div class="basic-section">
      <div>
        <div class="param-key">流程名称：</div>
        <jm-tooltip :content="workflowName" placement="bottom" effect="light">
          <div class="param-value">{{ workflowName }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">节点名称：</div>
        <jm-tooltip :content="task.nodeName" placement="bottom" effect="light">
          <div class="param-value">{{ task.nodeName }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">任务定义：</div>
        <jm-tooltip :content="task.defKey" placement="bottom" effect="light">
          <div class="param-value">{{ task.defKey }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">启动时间：</div>
        <jm-tooltip :content="datetimeFormatter(task.startTime)" placement="bottom" effect="light">
          <div class="param-value">{{ datetimeFormatter(task.startTime) }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">执行时长：</div>
        <jm-tooltip :content="executionTime" placement="bottom" effect="light">
          <div class="param-value">{{ executionTime }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">执行状态：</div>
        <div>
          <task-state :status="task.status"/>
        </div>
      </div>
    </div>

    <div class="tab-section">
      <jm-tabs v-model="tabActiveName" @tab-click="handleTabClick">
        <jm-tab-pane name="log" lazy>
          <template #label>
            <div class="tab">日志</div>
          </template>
          <div class="tab-content">
            <div class="log">
              <div class="loading" v-if="executing">
                <jm-button type="text" size="small" :loading="executing">
                  加载中...
                </jm-button>
              </div>
              <jm-log-viewer id="task-log" :filename="`${task.nodeName}.txt`" :value="taskLog"
                             :auto-scroll="taskLogAutoScroll"/>
            </div>
          </div>
        </jm-tab-pane>
        <jm-tab-pane name="params" lazy>
          <template #label>
            <div class="tab">业务参数</div>
          </template>
          <div class="tab-content">
            <div class="params" id="task-params">
              <jm-scrollbar>
                <div class="content">
                  <div class="title">输入参数</div>
                  <jm-table
                    :data="taskInputParams"
                    border>
                    <jm-table-column
                      label="参数唯一标识"
                      align="center"
                      prop="ref">
                    </jm-table-column>
                    <jm-table-column
                      label="参数类型"
                      align="center"
                      prop="valueType">
                    </jm-table-column>
                    <jm-table-column
                      label="参数值"
                      align="center"
                      prop="value">
                    </jm-table-column>
                  </jm-table>
                  <div class="title separator">输出参数</div>
                  <jm-table
                    :data="taskOutputParams"
                    border>
                    <jm-table-column
                      label="参数唯一标识"
                      align="center"
                      prop="ref">
                    </jm-table-column>
                    <jm-table-column
                      label="参数类型"
                      align="center"
                      prop="valueType">
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
import { computed, defineComponent, getCurrentInstance, onBeforeMount, onBeforeUnmount, ref } from 'vue';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { ITaskExecutionRecordVo, ITaskParamVo } from '@/api/dto/workflow-execution-record';
import TaskState from '@/views/workflow-execution-record/task-state.vue';
import { datetimeFormatter, executionTimeFormatter } from '@/utils/formatter';
import { checkTaskLog, fetchTaskLog, listTaskParam } from '@/api/view-no-auth';
import { adaptHeight, IAutoHeight } from '@/utils/auto-height';
import sleep from '@/utils/sleep';
import { TaskParamTypeEnum, TaskStatusEnum } from '@/api/dto/enumeration';
import { HttpError, TimeoutError } from '@/utils/rest/error';

const autoHeights: {
  [key: string]: IAutoHeight;
} = {
  log: {
    elementId: 'task-log',
    offsetTop: 286,
  },
  params: {
    elementId: 'task-params',
    offsetTop: 254,
  },
};

export default defineComponent({
  components: { TaskState },
  props: {
    id: {
      type: String,
      required: true,
    },
    tabType: {
      type: String,
      required: true,
    },
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const task = computed<ITaskExecutionRecordVo>(() => state.recordDetail.taskRecords.find(item => item.instanceId === props.id) as ITaskExecutionRecordVo);
    const executing = computed<boolean>(() => [TaskStatusEnum.INIT, TaskStatusEnum.WAITING, TaskStatusEnum.RUNNING].includes(task.value.status));
    const executionTime = computed<string>(() => executionTimeFormatter(task.value.startTime, task.value.endTime, executing.value));
    const tabActiveName = ref<string>(props.tabType);
    const taskLog = ref<string>('');
    const taskLogAutoScroll = ref<boolean>(false);
    const taskParams = ref<ITaskParamVo[]>([]);

    proxy.$nextTick(() => adaptHeight(autoHeights[tabActiveName.value]));

    let terminateTaskLogLoad = false;

    const loadTask = async (retry: number) => {
      if (terminateTaskLogLoad) {
        console.debug('组件已卸载，终止加载任务');
        return;
      }

      try {
        const headers: any = await checkTaskLog(props.id);
        const contentLength = +headers['content-length'] as number;

        if (contentLength > taskLog.value.length) {
          // 存在更多日志
          taskLog.value = await fetchTaskLog(props.id);
          taskLogAutoScroll.value = true;
        } else {
          console.debug('暂无更多日志');
          taskLogAutoScroll.value = false;
        }

        taskParams.value = await listTaskParam(props.id);
      } catch (err) {
        if (err instanceof TimeoutError) {
          // 忽略超时错误
          console.warn(err.message);
        } else if (err instanceof HttpError) {
          const { response: { status } } = err as HttpError;

          if (status !== 502) {
            throw err;
          }

          // 忽略502错误
          console.warn(err.message);
        }
      }

      if (!executing.value) {
        console.debug('任务已完成，终止获取日志');
        taskLogAutoScroll.value = false;
        return;
      }

      retry++;

      console.debug(`3秒后重试第${retry}次`);
      await sleep(3000);

      await loadTask(retry);
    };

    // 初始化任务
    onBeforeMount(() => loadTask(0));

    onBeforeUnmount(() => (terminateTaskLogLoad = true));

    return {
      workflowName: state.recordDetail.record?.name,
      task,
      executing,
      executionTime,
      tabActiveName,
      taskLog,
      taskLogAutoScroll,
      taskInputParams: computed<ITaskParamVo[]>(() => taskParams.value.filter(item => item.type === TaskParamTypeEnum.INPUT)),
      taskOutputParams: computed<ITaskParamVo[]>(() => taskParams.value.filter(item => item.type === TaskParamTypeEnum.OUTPUT)),
      datetimeFormatter,
      handleTabClick: ({ props: { name } }: any) => {
        proxy.$nextTick(() => adaptHeight(autoHeights[name]));
      },
    };
  },
});
</script>

<style scoped lang="less">
.workflow-execution-record-task-log {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;
  background-color: #FFFFFF;
  height: 100%;

  .basic-section {
    margin: 20px;
    padding: 16px 20px 0;
    display: flex;
    justify-content: space-between;
    box-shadow: 0 0 8px 0 #9EB1C5;

    > div {
      margin-bottom: 16px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      cursor: default;

      .param-key {
        color: #6B7B8D;
        margin-bottom: 8px;
      }

      .param-value,
      ::v-deep(.jm-state) {
        color: #082340;
      }

      .param-value {
        line-height: 25px;
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
        position: relative;

        .loading {
          position: absolute;
          top: 0;
          right: 0;
          z-index: 1;

          ::v-deep(.el-button) {
            padding-left: 5px;
            padding-right: 5px;
          }
        }
      }

      .params {
        background-color: #FFFFFF;
        border-radius: 4px;
        color: #082340;

        .content {
          padding: 0 16px 16px 16px;

          .title {
            padding: 16px 0;
            font-weight: 400;

            &.separator {
              margin-top: 16px;
              border-top: 1px solid #ECEEF6;
            }
          }

          ::v-deep(.el-table) {
            th, td {
              color: #082340;
            }
          }
        }
      }
    }
  }
}
</style>
