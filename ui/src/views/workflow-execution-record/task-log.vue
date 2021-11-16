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
        <div class="param-key">节点定义：</div>
        <jm-tooltip :content="nodeDef" placement="bottom" effect="light">
          <div class="param-value">{{ nodeDef }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">启动时间：</div>
        <jm-tooltip
          :content="datetimeFormatter(task.startTime)"
          placement="bottom"
          effect="light"
        >
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
              <jm-log-viewer
                id="task-log"
                :filename="`${task.nodeName}.txt`"
                :value="taskLog"
              />
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
                    :border="true"
                    :cell-class-name="
                      ({ row, columnIndex }) =>
                        row.required && columnIndex === 0 ? 'required-cell' : ''
                    "
                  >
                    <jm-table-column
                      label="参数唯一标识"
                      header-align="center"
                      width="200px"
                      prop="ref"
                    >
                      <template #default="scope">
                        <div>
                          <span>{{ scope.row.ref }}</span>
                          <jm-tooltip
                            content="必填项"
                            placement="top"
                            v-if="scope.row.required"
                          >
                            <img
                              src="~@/assets/svgs/task-log/required.svg"
                              alt=""
                            />
                          </jm-tooltip>
                        </div>
                      </template>
                    </jm-table-column>
                    <jm-table-column
                      header-align="center"
                      label="参数类型"
                      width="110px"
                      align="center"
                      prop="valueType"
                    >
                    </jm-table-column>
                    <jm-table-column
                      label="是/否必填"
                      header-align="center"
                      align="center"
                      width="110px"
                      prop="required"
                    >
                      <template #default="scope">
                        <span
                          :class="[scope.row.required ? 'is-required' : '']"
                        >
                          {{ scope.row.required ? '是' : '否' }}
                        </span>
                      </template>
                    </jm-table-column>
                    <jm-table-column
                      label="参数值"
                      prop="value"
                      header-align="center"
                    >
                    </jm-table-column>
                  </jm-table>
                  <div class="title separator">输出参数</div>
                  <jm-table :data="taskOutputParams" border>
                    <jm-table-column
                      header-align="center"
                      label="参数唯一标识"
                      width="200px"
                      prop="ref"
                    >
                      <template #default="scope">
                        <div>
                          <span>{{ scope.row.ref }}</span>
                          <jm-tooltip
                            content="必填项"
                            placement="top"
                            v-if="scope.row.required"
                          >
                            <img
                              src="~@/assets/svgs/task-log/required.svg"
                              alt=""
                            />
                          </jm-tooltip>
                        </div>
                      </template>
                    </jm-table-column>
                    <jm-table-column
                      header-align="center"
                      label="参数类型"
                      align="center"
                      width="110px"
                      prop="valueType"
                    >
                    </jm-table-column>
                    <jm-table-column
                      header-align="center"
                      label="是/否必填"
                      width="110px"
                      align="center"
                      prop="required"
                    >
                      <template #default="scope">
                        <span
                          :class="[scope.row.required ? 'is-required' : '']"
                        >
                          {{ scope.row.required ? '是' : '否' }}
                        </span>
                      </template>
                    </jm-table-column>
                    <jm-table-column
                      label="参数值"
                      prop="value"
                      header-align="center"
                    >
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
import { SHELL_NODE_TYPE } from '@/components/workflow/workflow-viewer/utils/model';

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
    const task = computed<ITaskExecutionRecordVo>(
      () =>
        state.recordDetail.taskRecords.find(
          item => item.instanceId === props.id,
        ) as ITaskExecutionRecordVo,
    );
    const executing = computed<boolean>(() =>
      [
        TaskStatusEnum.INIT,
        TaskStatusEnum.WAITING,
        TaskStatusEnum.RUNNING,
      ].includes(task.value.status),
    );
    const executionTime = computed<string>(() =>
      executionTimeFormatter(
        task.value.startTime,
        task.value.endTime,
        executing.value,
      ),
    );
    const tabActiveName = ref<string>(props.tabType);
    const taskLog = ref<string>('');
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
        } else {
          console.debug('暂无更多日志');
        }

        taskParams.value = await listTaskParam(props.id);
      } catch (err) {
        if (err instanceof TimeoutError) {
          // 忽略超时错误
          console.warn(err.message);
        } else if (err instanceof HttpError) {
          const { response } = err as HttpError;

          if (response && response.status !== 502) {
            throw err;
          }

          // 忽略错误
          console.warn(err.message);
        }
      }

      if (!executing.value) {
        console.debug('任务已完成，终止获取日志');
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
      nodeDef: computed<string>(() => task.value.defKey.startsWith(`${SHELL_NODE_TYPE}:`) ? SHELL_NODE_TYPE : task.value.defKey),
      taskInputParams: computed<ITaskParamVo[]>(() =>
        taskParams.value.filter(item => item.type === TaskParamTypeEnum.INPUT),
      ),
      taskOutputParams: computed<ITaskParamVo[]>(() =>
        taskParams.value.filter(item => item.type === TaskParamTypeEnum.OUTPUT),
      ),
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
  background-color: #ffffff;
  height: 100%;

  .basic-section {
    margin: 20px;
    padding: 16px 20px 0;
    display: flex;
    justify-content: space-between;
    box-shadow: 0 0 8px 0 #9eb1c5;

    > div {
      margin-bottom: 16px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      cursor: default;

      .param-key {
        color: #6b7b8d;
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

      ::v-deep(.el-table) {
        td {
          &.required-cell {
            .cell {
              padding-right: 40px;
            }
          }

          .cell {
            padding: 0 20px;

            div {
              display: inline-block;
              max-width: 100%;
              position: relative;

              span {
                display: inline-block;
                max-width: 100%;
                text-overflow: ellipsis;
                overflow: hidden;
                white-space: nowrap;
              }

              img {
                position: absolute;
                right: -20px;
                bottom: 10px;
              }
            }
          }

          &.is-center {
            .cell {
              padding: 0px 10px;
            }
          }
        }

        .cell {
          .is-required {
            color: #ff0000;
          }
        }
      }

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
        background-color: #ffffff;
        border-radius: 4px;
        color: #082340;

        .content {
          padding: 0 16px 16px 16px;

          .title {
            padding: 16px 0;
            font-weight: 400;

            &.separator {
              margin-top: 16px;
              border-top: 1px solid #eceef6;
            }
          }

          ::v-deep(.el-table) {
            th,
            td {
              color: #082340;
            }
          }
        }
      }
    }
  }
}
</style>
