<template>
  <div class="workflow-execution-record-task-log">
    <div class="basic-section">
      <div class="item">
        <div>
          <div class="param-key">流程名称</div>
          <div class="param-value">
            <jm-text-viewer :value="workflowName" :tip-append-to-body="false" />
          </div>
        </div>
        <div class="param-number" v-if="tasks.length > 1">
          <div class="title">执行次数</div>
          <div class="total times">{{ statusParams.total }}</div>
        </div>
      </div>
      <div class="item">
        <div>
          <div class="param-key">节点名称</div>
          <div class="param-value">
            <jm-text-viewer :value="nodeName" :tip-append-to-body="false" />
          </div>
        </div>
        <div class="param-number" v-if="tasks.length > 1">
          <div class="title">成功次数</div>
          <div class="success times">{{ statusParams.successNum }}</div>
        </div>
      </div>
      <div class="item">
        <div>
          <div class="param-key">节点定义</div>
          <div class="param-value">
            <jm-text-viewer :value="nodeDef" :tip-append-to-body="false" />
          </div>
        </div>
        <div class="param-number" v-if="tasks.length > 1">
          <div class="title">失败次数</div>
          <div class="fail times">{{ statusParams.failNum }}</div>
        </div>
      </div>
      <div class="item">
        <div>
          <div class="param-key">启动时间</div>
          <div class="param-value">
            <jm-text-viewer :value="datetimeFormatter(task.startTime)" :tip-append-to-body="false" />
          </div>
        </div>
        <div class="param-number" v-if="tasks.length > 1">
          <div class="title">跳过次数</div>
          <div class="skip times">{{ statusParams.skipNum }}</div>
        </div>
      </div>
      <div class="item">
        <div>
          <div class="param-key">执行时长</div>
          <div class="param-value">
            <jm-timer :start-time="task.startTime" :end-time="task.endTime" :tip-append-to-body="false" />
          </div>
        </div>
        <div class="param-number" v-if="tasks.length > 1">
          <div class="title">挂起次数</div>
          <div class="suspend times">{{ statusParams.suspendNum }}</div>
        </div>
      </div>
      <div class="item">
        <div>
          <div class="param-key">执行状态</div>
          <div>
            <jm-task-state :value="task.status" />
          </div>
        </div>
        <div class="param-number" v-if="tasks.length > 1">
          <div class="title">忽略次数</div>
          <div class="ignore times">{{ statusParams.ignoreNum }}</div>
        </div>
      </div>
    </div>
    <div class="tab-section">
      <task-list :tasks="tasks" @change="changeTask" />
      <jm-tabs v-model="tabActiveName">
        <jm-tab-pane name="log" lazy>
          <template #label>
            <div class="tab">日志</div>
          </template>
          <div class="tab-content">
            <div :class="[tasks.length > 1 ? 'tasks-log' : 'task-log']" class="log">
              <div class="loading" v-if="executing">
                <jm-button type="text" size="small" :loading="executing"> 加载中...</jm-button>
              </div>
              <jm-log-viewer
                v-if="currentInstanceId"
                :filename="`${task.nodeName}.txt`"
                :url="`/view/logs/task/subscribe/${currentInstanceId}?size=`"
                v-model:more="moreLog"
                :download="download"
              />
            </div>
          </div>
        </jm-tab-pane>
        <jm-tab-pane name="params" lazy>
          <template #label>
            <div class="tab">业务参数</div>
          </template>
          <div class="tab-content">
            <div :class="[tasks.length > 1 ? 'tasks-params' : 'task-params']" class="params">
              <jm-scrollbar>
                <div class="content">
                  <div class="title">输入参数</div>
                  <jm-table
                    :data="taskInputParams"
                    :border="true"
                    :cell-class-name="
                      ({ row, columnIndex }) => (row.required && columnIndex === 0 ? 'required-cell' : '')
                    "
                  >
                    <jm-table-column label="参数唯一标识" header-align="center" width="200px" prop="ref">
                      <template #default="scope">
                        <div
                          :style="{
                            maxWidth: maxWidthRecord[scope.row.ref] ? `${maxWidthRecord[scope.row.ref]}px` : '100%',
                          }"
                        >
                          <div class="text-viewer">
                            <jm-text-viewer
                              :value="scope.row.ref"
                              class="value"
                              :tip-append-to-body="false"
                              @loaded="({ contentMaxWidth }) => getTotalWidth(contentMaxWidth, scope.row.ref)"
                            />
                          </div>
                          <jm-tooltip content="必填项" placement="top" :appendToBody="false" v-if="scope.row.required">
                            <img src="~@/assets/svgs/task-log/required.svg" alt="" />
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
                        <span :class="[scope.row.required ? 'is-required' : '']">
                          {{ scope.row.required ? '是' : '否' }}
                        </span>
                      </template>
                    </jm-table-column>
                    <jm-table-column label="参数值" header-align="center">
                      <template #default="scope">
                        <param-value :value="scope.row.value" :tip-append-to-body="false" :type="scope.row.valueType" />
                      </template>
                    </jm-table-column>
                  </jm-table>
                  <div class="title separator">输出参数</div>
                  <jm-table
                    :data="taskOutputParams"
                    :border="true"
                    :cell-class-name="
                      ({ row, columnIndex }) => (row.required && columnIndex === 0 ? 'required-cell' : '')
                    "
                  >
                    <jm-table-column header-align="center" label="参数唯一标识" width="200px" prop="ref">
                      <template #default="scope">
                        <div
                          :style="{
                            maxWidth: maxWidthRecord[scope.row.ref] ? `${maxWidthRecord[scope.row.ref]}px` : '100%',
                          }"
                        >
                          <div class="text-viewer">
                            <jm-text-viewer
                              :value="scope.row.ref"
                              class="value"
                              :tip-append-to-body="false"
                              @loaded="({ contentMaxWidth }) => getTotalWidth(contentMaxWidth, scope.row.ref)"
                            />
                          </div>
                          <jm-tooltip content="必填项" placement="top" :appendToBody="false" v-if="scope.row.required">
                            <img src="~@/assets/svgs/task-log/required.svg" alt="" />
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
                        <span :class="[scope.row.required ? 'is-required' : '']">
                          {{ scope.row.required ? '是' : '否' }}
                        </span>
                      </template>
                    </jm-table-column>
                    <jm-table-column label="参数值" header-align="center">
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
        <jm-tab-pane name="cache" v-if="showCache" lazy>
          <template #label>
            <div class="tab">缓存</div>
          </template>
          <div class="tab-content">
            <div class="params">
              <jm-scrollbar>
                <div class="content">
                  <div class="cache-table">
                    <jm-table :data="nodeCaches" :border="true">
                      <jm-table-column header-align="center" label="缓存" prop="name">
                        <template #default="scope">
                          <jm-text-viewer :value="scope.row.name" />
                        </template>
                      </jm-table-column>
                      <jm-table-column label="挂载目录" header-align="center" prop="path">
                        <template #default="scope">
                          <jm-text-viewer :value="scope.row.path" />
                        </template>
                      </jm-table-column>
                      <jm-table-column header-align="center" class-name="status" prop="available">
                        <template #header>
                          <div class="wrapper">
                            <span>状态</span>
                            <jm-tooltip>
                              <template #content>
                                <p>对于不可用的缓存，可到项目卡片-</p>
                                <p style="margin-top: 5px">更多-缓存中清理或项目编辑页删除</p>
                              </template>
                              <i class="icon jm-icon-button-help"></i>
                            </jm-tooltip>
                          </div>
                        </template>
                        <template #default="scope">
                          <div class="enable" v-if="scope.row.available">
                            可用<i class="jm-icon-button-success icon"></i>
                          </div>
                          <div class="disabled" v-else>不可用<i class="jm-icon-button-warning icon"></i></div>
                        </template>
                      </jm-table-column>
                    </jm-table>
                  </div>
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
import {
  computed,
  defineComponent,
  getCurrentInstance,
  nextTick,
  onBeforeMount,
  onBeforeUnmount,
  onUpdated,
  ref,
} from 'vue';
import { useStore } from 'vuex';
import yaml from 'yaml';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { ITaskExecutionRecordVo, ITaskParamVo } from '@/api/dto/workflow-execution-record';
import TaskList from '@/views/workflow-execution-record/task-list.vue';
import { datetimeFormatter } from '@/utils/formatter';
import { fetchNodeCache, listTaskInstance, listTaskParam } from '@/api/view-no-auth';
import sleep from '@/utils/sleep';
import { ParamTypeEnum, TaskParamTypeEnum, TaskStatusEnum } from '@/api/dto/enumeration';
import { HttpError, TimeoutError } from '@/utils/rest/error';
import { SHELL_NODE_TYPE } from '@/components/workflow/workflow-viewer/model/data/common';
import { sortTasks } from '@/components/workflow/workflow-viewer/model/util';
import { downloadNodeLogs } from '@/api/workflow-execution-record';
import ParamValue from '@/views/common/param-value.vue';
import { INodeCacheVo } from '@/api/dto/cache';

export default defineComponent({
  components: { TaskList, ParamValue },
  props: {
    dsl: {
      type: String,
      required: true,
    },
    businessId: {
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
    const { pipeline, workflow } = yaml.parse(props.dsl);
    const state = useStore().state[namespace] as IState;
    const taskInstances = ref<ITaskExecutionRecordVo[]>([]);
    const currentInstanceId = ref<string>('');
    // 切换时状态
    const switchStatus = ref<TaskStatusEnum>();
    // 初始id
    const firstId = ref<string>();
    const asyncTask = computed<ITaskExecutionRecordVo>(() => {
      const latestTaskInstanceId = taskInstances.value.length === 0 ? '' : taskInstances.value[0].instanceId;
      const at = state.recordDetail.taskRecords.find(item => item.businessId === props.businessId) || {
        instanceId: '',
        businessId: '',
        nodeName: '',
        defKey: '',
        startTime: '',
        status: TaskStatusEnum.INIT,
      };
      return {
        ...at,
        instanceId: latestTaskInstanceId,
      };
    });
    const taskInstanceId = ref<string>('');
    const task = computed<ITaskExecutionRecordVo>(() => {
      const index = taskInstances.value.findIndex(({ instanceId }) => instanceId === taskInstanceId.value);
      const t = {
        ...(index === -1 ? asyncTask.value : taskInstances.value[index]),
      };
      if (index === 0) {
        // 最后一个任务时，用异步任务状态
        t.startTime = asyncTask.value.startTime;
        t.endTime = asyncTask.value.endTime;
        t.status = asyncTask.value.status;
      }
      return t;
    });
    // 控制缓存tab显隐
    const showCache = computed<boolean>(() => !!task.value.taskCaches);
    const getTasks = () => {
      if (taskInstances.value.length === 0) {
        return [];
      }

      const arr: ITaskExecutionRecordVo[] = [];
      arr.push(
        {
          ...asyncTask.value,
        },
        ...taskInstances.value.slice(1),
      );

      firstId.value = arr[0].instanceId;
      return arr;
    };
    const tasks = computed<ITaskExecutionRecordVo[]>(getTasks);
    const executing = computed<boolean>(() => {
      if (switchStatus.value) {
        return [TaskStatusEnum.INIT, TaskStatusEnum.WAITING, TaskStatusEnum.RUNNING].includes(switchStatus.value);
      } else {
        return [TaskStatusEnum.INIT, TaskStatusEnum.WAITING, TaskStatusEnum.RUNNING].includes(task.value.status);
      }
    });
    const isSuspended = computed<boolean>(() => task.value.status === TaskStatusEnum.SUSPENDED);
    const tabActiveName = ref<string>(props.tabType);
    const taskParams = ref<ITaskParamVo[]>([]);
    // 节点缓存
    const nodeCaches = ref<INodeCacheVo[]>([]);
    const moreLog = ref<boolean>(false);
    // 运行状态次数
    const statusParams = computed<{
      total: number;
      successNum: number;
      failNum: number;
      skipNum: number;
      suspendNum: number;
      ignoreNum: number;
    }>(() => {
      const statusNum = {
        total: 0,
        successNum: 0,
        failNum: 0,
        skipNum: 0,
        suspendNum: 0,
        ignoreNum: 0,
      };

      tasks.value.forEach(item => {
        if (item.status === TaskStatusEnum.SUCCEEDED) {
          statusNum.successNum++;
        } else if (item.status === TaskStatusEnum.FAILED) {
          statusNum.failNum++;
        } else if (item.status === TaskStatusEnum.SKIPPED) {
          statusNum.skipNum++;
        } else if (item.status === TaskStatusEnum.SUSPENDED) {
          statusNum.suspendNum++;
        } else if (item.status === TaskStatusEnum.IGNORED) {
          statusNum.ignoreNum++;
        }
      });
      statusNum.total = tasks.value.length;

      return statusNum;
    });

    const loadData = async (func: (id: string) => Promise<void>, id: string, retry = 0) => {
      if (!taskInstanceId.value || taskInstanceId.value !== id) {
        console.debug('组件已卸载，终止加载任务');
        return;
      }

      try {
        await func(id);
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
        console.debug('任务已完成，终止获取日志或参数');
        return;
      }

      retry++;

      console.debug(`3秒后重试第${retry}次`);
      await sleep(3000);

      await loadData(func, id, retry);
    };

    const initialize = (id: string) => {
      taskInstanceId.value = id;
      currentInstanceId.value = id;

      // 加载参数
      loadData(async (id: string) => {
        taskParams.value = await listTaskParam(id);
      }, id);
    };

    const loadNodeCaches = async () => {
      try {
        nodeCaches.value = await fetchNodeCache(props.businessId);
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };

    const destroy = () => {
      taskInstanceId.value = '';
    };

    // 初始化任务
    onBeforeMount(async () => {
      taskInstances.value = sortTasks(await listTaskInstance(props.businessId), true);
      await loadNodeCaches();
      if (taskInstances.value.length > 0) {
        initialize(taskInstances.value[0].instanceId);
        currentInstanceId.value = taskInstances.value[0].instanceId;
      }
    });

    // 销毁任务
    onBeforeUnmount(destroy);

    const maxWidthRecord = ref<Record<string, number>>({});
    const changeTask = async (instanceId: string) => {
      // 添加判断避免初始化时调用，导致判断task状态失效
      if (firstId.value !== instanceId) {
        tasks.value.forEach(item => {
          if (item.instanceId === instanceId) {
            switchStatus.value = item.status;
          }
        });
      } else {
        // 如果是初始化或选择下标0执行task部分逻辑
        switchStatus.value = undefined;
      }
      taskParams.value = await listTaskParam(instanceId);
      await nextTick();
      currentInstanceId.value = instanceId;
    };

    const asyncTaskStartTime = ref<string>('');
    onUpdated(async () => {
      if (asyncTask.value.startTime === asyncTaskStartTime.value) {
        return;
      }
      // 开始时间变化时，表示开始新任务
      if (asyncTaskStartTime.value) {
        taskInstances.value = sortTasks(await listTaskInstance(props.businessId), true);
        if (!taskInstanceId.value && taskInstances.value.length > 0) {
          await changeTask(taskInstances.value[0].instanceId);
        }
      }
      asyncTaskStartTime.value = asyncTask.value.startTime;
    });
    // 下载日志
    const download = async () => {
      try {
        return await downloadNodeLogs(currentInstanceId.value);
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };
    return {
      showCache,
      nodeCaches,
      ParamTypeEnum,
      maxWidthRecord,
      workflowName: state.recordDetail.record?.name,
      taskInstanceId,
      task,
      tasks,
      executing,
      isSuspended,
      tabActiveName,
      moreLog,
      nodeName: computed<string>(() => {
        const { alias } = (pipeline || workflow)[task.value.nodeName];
        return alias || task.value.nodeName;
      }),
      download,
      currentInstanceId,
      nodeDef: computed<string>(() =>
        task.value.defKey.startsWith(`${SHELL_NODE_TYPE}:`) ? SHELL_NODE_TYPE : task.value.defKey,
      ),
      taskInputParams: computed<ITaskParamVo[]>(() =>
        taskParams.value
          .filter(item => item.type === TaskParamTypeEnum.INPUT)
          .sort((p1, p2) => p1.ref.localeCompare(p2.ref)),
      ),
      taskOutputParams: computed<ITaskParamVo[]>(() => {
        const params = taskParams.value
          .filter(({ ref, type }) => ref && !ref.startsWith('inner.') && type === TaskParamTypeEnum.OUTPUT)
          .sort((p1, p2) => p1.ref.localeCompare(p2.ref));

        params.push(
          ...taskParams.value
            .filter(({ ref, type }) => ref && ref.startsWith('inner.') && type === TaskParamTypeEnum.OUTPUT)
            .sort((p1, p2) => p1.ref.localeCompare(p2.ref)),
        );

        return params;
      }),
      datetimeFormatter,
      getTotalWidth(width: number, ref: string) {
        maxWidthRecord.value[ref] = width;
      },
      TaskStatusEnum,
      changeTask,
      statusParams,
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
    margin: 20px 24px;
    display: flex;
    justify-content: space-between;
    border-bottom: 1px solid #e6ebf2;

    > div {
      &.item {
        flex: 1;

        .param-number {
          margin-top: 20px;

          .title {
            margin-bottom: 12px;
            font-size: 14px;
            color: #6b7b8d;
          }

          .times {
            font-weight: 500;
          }

          .success {
            color: #3ebb03;
          }

          .fail {
            color: #cf1322;
          }

          .skip {
            color: #979797;
          }

          .suspend {
            color: #7986cb;
          }

          .ignore {
            color: #9847fc;
          }
        }
      }

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
        margin-right: 10px;
      }
    }
  }

  .tab-section {
    margin: 0 20px;
    position: relative;

    .task-list {
      position: absolute;
      right: 0;
      top: 9px;
      z-index: 2;
    }

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
        overflow: visible;

        .icon {
          width: 16px;
          height: 16px;
          font-size: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          overflow: hidden;

          &:before {
            margin: 0;
          }
        }

        .el-table__header-wrapper {
          thead tr th .cell .wrapper {
            display: flex;
            justify-content: center;
            align-items: center;

            .icon {
              color: #6b7b8d;
              margin-left: 4px;
            }
          }
        }

        .el-table__body-wrapper {
          overflow: visible;

          td {
            &.required-cell {
              .cell {
                padding-right: 40px;
              }
            }

            .cell {
              padding: 0 20px;

              .text-viewer {
                display: flex;
                align-items: center;

                &.param-value {
                  .value {
                    width: 100%;
                  }
                }

                .value {
                  width: 100%;
                }
              }

              & > div {
                display: inline-block;
                width: 100%;
                position: relative;

                img {
                  position: absolute;
                  left: 100%;
                  margin-left: 5px;
                  bottom: 0;
                }
              }
            }

            &.is-center {
              .cell {
                padding: 0px 10px;
              }
            }

            &.status {
              .cell {
                font-weight: 400;
                font-size: 14px;
                line-height: 20px;
                display: flex;
                justify-content: center;

                .enable,
                .disabled {
                  display: flex;
                  align-items: center;

                  .icon {
                    margin-left: 2px;
                  }
                }

                .enable {
                  padding: 2px 4px 2px 8px;
                  width: 58px;
                  height: 24px;
                  background: rgba(62, 187, 3, 0.15);
                  border-radius: 12px;
                  color: #3ebb03;
                }

                // 不可用演示
                .disabled {
                  padding: 2px 6px 2px 8px;
                  width: 74px;
                  height: 24px;
                  background: rgba(236, 77, 77, 0.1);
                  border-radius: 12px;
                  color: #ec4d4d;
                }
              }
            }
          }

          .cell {
            overflow: visible;

            .is-required {
              color: #ff0000;
            }
          }
        }
      }

      .task-log {
        height: calc(100vh - 260px);
      }

      .tasks-log {
        height: calc(100vh - 330px);
      }

      .cache-table {
        padding: 20px 0 0;
      }

      .log {
        margin: 16px;
        position: relative;
        //height: calc(100vh - 286px);

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

      .task-params {
        height: calc(100vh - 228px);
      }

      .tasks-params {
        height: calc(100vh - 298px);
      }

      .params {
        background-color: #ffffff;
        border-radius: 4px;
        color: #082340;
        //height: calc(100vh - 254px);

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
