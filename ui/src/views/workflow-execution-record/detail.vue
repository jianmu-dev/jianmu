<template>
  <div class="workflow-execution-record-detail" v-loading="loading">
    <div class="basic-section">
      <button
        v-if="showStopAll"
        @click="terminateAllRecord"
        class="all-stop jm-icon-button-stop"
        :class="[clicked ? 'clicked' : '']"
        @keypress.enter.prevent
      >
        终止全部
      </button>
      <!-- <jm-tooltip :content="clicked ? '暂无法点击' : '终止全部'" placement="left">
      </jm-tooltip> -->
      <jm-tooltip content="触发" placement="left">
        <button class="trigger-btn jm-icon-button-on" @click="execute" @keypress.enter.prevent></button>
      </jm-tooltip>
      <div class="info">
        <div class="name">
          <i class="jm-icon-button-left back" @click="goBack"></i>
          <span>{{ data.record?.name }}</span>
          <router-link
            :to="{
              path: `/project-group/detail/${data.project?.projectGroupId}`,
            }"
          >
            <span class="project-group-name">{{ data.project?.projectGroupName }}</span>
          </router-link>
        </div>
        <div class="desc" v-html="data.record?.description?.replace(/\n/g, '<br/>')"></div>
      </div>
      <div v-if="!data.record?.status" class="instance-tabs">
        <div class="tab init selected">
          <div class="right-horn" />
          <div class="label">-</div>
        </div>
      </div>
      <jm-scrollbar v-else ref="navScrollBar">
        <div class="instance-tabs">
          <div
            v-for="record of data.allRecords"
            :key="record.id"
            :class="{
              tab: true,
              [record.id === data.record.id ? 'selected' : 'unselected']: true,
              [record.status.toLowerCase()]: true,
            }"
            @click="changeRecord(record)"
          >
            <div v-if="record.id === data.record.id" class="left-horn" />
            <div v-if="record.id === data.record.id" class="right-horn" />
            <div class="label">{{ record.serialNo }}</div>
          </div>
        </div>
      </jm-scrollbar>
      <div
        :class="{
          'instance-tab-content': true,
          [!data.record?.status ? 'init' : data.record.status.toLowerCase()]: true,
        }"
      >
        <div class="item">
          <div class="value">
            {{ datetimeFormatter(data.record?.startTime) }}
          </div>
          <div>启动时间</div>
        </div>
        <div class="item">
          <div class="value">{{ datetimeFormatter(data.record?.endTime) }}</div>
          <div>完成时间</div>
        </div>
        <div class="item" v-if="data.record?.status === WorkflowExecutionRecordStatusEnum.SUSPENDED">
          <jm-timer class="value" :start-time="data.record?.suspendedTime" />
          <div>挂起时长</div>
        </div>
        <div class="item" v-else>
          <jm-timer class="value" :start-time="data.record?.startTime" :end-time="data.record?.endTime" />
          <div>执行时长</div>
        </div>
        <div class="item">
          <div v-if="!data.record?.id" class="value">无</div>
          <jm-text-viewer v-else :value="data.record?.id" class="value" />
          <div>流程实例ID</div>
        </div>
        <div class="item">
          <jm-text-viewer :value="data.record?.workflowVersion || '无'" class="value" />
          <div>流程版本号</div>
        </div>
        <jm-tooltip v-if="checkWorkflowRunning(data.record?.status)" content="终止" placement="left">
          <button
            :class="{
              'terminate-btn': true,
              'jm-icon-button-stop': true,
              [!data.record?.status ? 'init' : data.record.status.toLowerCase()]: true,
            }"
            @click="terminate"
            @keypress.enter.prevent
          ></button>
        </jm-tooltip>
      </div>
    </div>

    <div class="workflow-section">
      <workflow />
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, inject, onBeforeUnmount, onMounted, provide, ref } from 'vue';
import { createNamespacedHelpers, useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { datetimeFormatter } from '@/utils/formatter';
import { TaskStatusEnum, TriggerTypeEnum, WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import Workflow from '@/views/workflow-execution-record/workflow.vue';
import { ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { executeImmediately } from '@/api/project';
import sleep from '@/utils/sleep';
import { onBeforeRouteUpdate, useRouter } from 'vue-router';
import { terminate, terminateAll } from '@/api/workflow-execution-record';
import { HttpError, TimeoutError } from '@/utils/rest/error';
import { IProjectDetailVo } from '@/api/dto/project';
import { IRootState } from '@/model';

const { mapActions, mapMutations } = createNamespacedHelpers(namespace);

export default defineComponent({
  components: { Workflow },
  props: {
    projectId: {
      type: String,
      required: true,
    },
    workflowExecutionRecordId: String,
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const store = useStore();
    const rootState = store.state as IRootState;
    const state = store.state[namespace] as IState;
    const loading = ref<boolean>(false);
    const reloadMain = inject('reloadMain') as () => void;
    const navScrollBar = ref();
    let terminateLoad = false;

    const loadData = async (refreshing?: boolean) => {
      try {
        await proxy.fetchDetail({
          projectId: props.projectId,
          workflowExecutionRecordId: props.workflowExecutionRecordId,
        });
      } catch (err) {
        if (!refreshing) {
          throw err;
        }

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
    };
    provide('loadData', loadData);

    const checkWorkflowRunning = (status: WorkflowExecutionRecordStatusEnum): boolean => {
      return [
        WorkflowExecutionRecordStatusEnum.INIT,
        WorkflowExecutionRecordStatusEnum.RUNNING,
        WorkflowExecutionRecordStatusEnum.SUSPENDED,
      ].includes(status);
    };

    const checkTaskRunning = (status: TaskStatusEnum): boolean => {
      return [TaskStatusEnum.WAITING, TaskStatusEnum.RUNNING, TaskStatusEnum.SUSPENDED].includes(status);
    };

    const loadDetail = async (refreshing?: boolean) => {
      if (terminateLoad) {
        console.debug('组件已卸载，终止刷新');
        return;
      }

      try {
        if (!refreshing) {
          loading.value = !loading.value;
        }

        await loadData(refreshing);
        if (!refreshing) {
          loading.value = !loading.value;
        }

        const { status } = state.recordDetail.record as IWorkflowExecutionRecordVo;

        if (
          checkWorkflowRunning(status) ||
          state.recordDetail.taskRecords.find(item => checkTaskRunning(item.status))
        ) {
          console.debug('3秒后刷新');
          await sleep(3000);
          await loadDetail(true);
        }
      } catch (err) {
        if (!refreshing) {
          loading.value = !loading.value;
        }

        proxy.$throw(err, proxy);
      }
    };

    onBeforeRouteUpdate((to, { name }) => {
      if (name !== 'workflow-execution-record-detail') {
        return;
      }

      // 保留滚动偏移量
      proxy.mutateNavScrollLeft(navScrollBar.value.scrollbar.firstElementChild.scrollLeft);
    });

    // 初始化流程执行记录详情
    onMounted(async () => {
      await loadDetail();

      // 初始化滚动偏移量
      if (state.recordDetail.project?.id === props.projectId && props.workflowExecutionRecordId && navScrollBar.value) {
        if (state.recordDetail.navScrollLeft === 0) {
          const index = state.recordDetail.allRecords.findIndex(({ id }) => id === props.workflowExecutionRecordId);
          const contentWidth = navScrollBar.value.scrollbar.firstElementChild.clientWidth;
          const navScrollLeft =
            navScrollBar.value.scrollbar.firstElementChild.firstElementChild.firstElementChild.children.item(
              index,
            ).offsetLeft;
          if (navScrollLeft > contentWidth) {
            const maxNavScrollBarLeft =
              navScrollBar.value.scrollbar.firstElementChild.scrollWidth -
              navScrollBar.value.scrollbar.firstElementChild.clientWidth;
            proxy.mutateNavScrollLeft(navScrollLeft > maxNavScrollBarLeft ? maxNavScrollBarLeft : navScrollLeft);
          }
        }
        navScrollBar.value.scrollbar.firstElementChild.scrollLeft = state.recordDetail.navScrollLeft;
      } else {
        proxy.mutateNavScrollLeft(0);
      }
    });

    onBeforeUnmount(() => {
      terminateLoad = true;

      // 清空数据
      proxy.mutateRecordDetail({});
    });

    const data = computed<{
      project?: IProjectDetailVo;
      allRecords: IWorkflowExecutionRecordVo[];
      record?: IWorkflowExecutionRecordVo;
      taskRecords: ITaskExecutionRecordVo[];
    }>(() => state.recordDetail);

    const showStopAll = computed<boolean>(
      () =>
        data.value.allRecords.filter(
          e =>
            e.status === WorkflowExecutionRecordStatusEnum.RUNNING ||
            e.status === WorkflowExecutionRecordStatusEnum.SUSPENDED,
        ).length >= 2,
    );
    const clicked = ref<boolean>(false);
    return {
      navScrollBar,
      WorkflowExecutionRecordStatusEnum,
      data,
      showStopAll,
      clicked,
      loading,
      ...mapMutations({
        mutateRecordDetail: 'mutateRecordDetail',
        mutateNavScrollLeft: 'mutateRecordDetailNavScrollLeft',
      }),
      ...mapActions({
        fetchDetail: 'fetchDetail',
      }),
      close: () => {
        router.push(rootState.fromRoute.fullPath);
      },
      changeRecord: async (record: IWorkflowExecutionRecordVo) => {
        const { id } = record;

        if (!data.value.record || data.value.record.id === id) {
          // 忽略
          return;
        }

        await router.push({
          name: 'workflow-execution-record-detail',
          query: {
            projectId: props.projectId,
            workflowExecutionRecordId: id,
          },
        });

        // 刷新详情
        reloadMain();
      },
      datetimeFormatter,
      checkWorkflowRunning,

      execute: () => {
        const isWarning = data.value.project?.triggerType === TriggerTypeEnum.WEBHOOK;
        let msg = '<div>确定要触发吗?</div>';
        if (isWarning) {
          msg +=
            '<div style="color: red; margin-top: 5px; font-size: 12px; line-height: normal;">注意：项目已配置webhook，手动触发可能会导致不可预知的结果，请慎重操作。</div>';
        }

        proxy
          .$confirm(msg, '触发项目执行', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: isWarning ? 'warning' : 'info',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            executeImmediately(props.projectId)
              .then(async () => {
                proxy.$success('操作成功');

                // 清除滚动偏移量
                proxy.mutateNavScrollLeft(0);

                await router.push({
                  name: 'workflow-execution-record-detail',
                  query: {
                    projectId: props.projectId,
                  },
                });

                // 刷新详情
                reloadMain();
              })
              .catch((err: Error) => proxy.$throw(err, proxy));
          });
      },
      terminateAllRecord: () => {
        proxy
          .$confirm('确定要终止执行中/挂起的全部实例吗？', '终止全部', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
          })
          .then((type: any) => {
            if (clicked.value) return;
            if (type === 'confirm') {
              clicked.value = true;
            }
            if (!data.value.record) {
              return;
            }
            terminateAll(data.value.record.workflowRef)
              .then(() => {
                proxy.$success('操作成功，正在终止，请稍后');
                // 刷新详情
                reloadMain();
              })
              .catch((err: Error) => proxy.$throw(err, proxy))
              .finally(() => {
                setTimeout(() => {
                  clicked.value = false;
                }, 6000);
              });
          });
      },
      terminate: () => {
        proxy
          .$confirm('确定要终止吗?', '终止项目执行', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
          })
          .then(() => {
            if (!data.value.record) {
              return;
            }

            terminate(data.value.record.id)
              .then(() => {
                proxy.$success('终止成功');

                // 刷新详情
                reloadMain();
              })
              .catch((err: Error) => proxy.$throw(err, proxy));
          });
      },
      goBack() {
        const { fullPath } = rootState.fromRoute;
        router.push(fullPath);
      },
    };
  },
});
</script>

<style scoped lang="less">
@primary-color: #096dd9;
@secondary-color: #0091ff;

.workflow-execution-record-detail {
  font-size: 14px;
  color: #333333;
  margin-bottom: 20px;

  .basic-section {
    position: relative;
    margin-bottom: 16px;
    padding-top: 16px;
    background: #ffffff;

    .all-stop {
      position: absolute;
      right: 97px;
      top: 20px;
      width: 98px;
      height: 36px;
      border: 1px solid #cad6ee;
      border-radius: 2px;
      color: #042749;
      background-color: #ffffff;
      cursor: pointer;
      &::before {
        font-size: 16px;
        margin-right: 6px;
      }
      &::after {
        position: relative;
        content: '';
        right: -32px;
        height: 16px;
        border: 1px solid #cdd1e3;
        pointer-events: none;
      }
    }
    .clicked {
      cursor: no-drop;
    }
    .trigger-btn {
      position: absolute;
      right: 12px;
      top: 12px;
      width: 50px;
      height: 50px;
      border-radius: 4px;
      font-size: 36px;
      border: 0;
      background-color: transparent;
      color: #6b7b8d;
      cursor: pointer;
      outline: none;

      &:active {
        background-color: #eff7ff;
      }
    }

    .info {
      padding: 0 24px;
      margin-bottom: 14px;

      .name {
        font-size: 20px;
        font-weight: bold;
        color: #082340;
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .back {
          cursor: pointer;
          width: 20px;
          height: 20px;
          line-height: 20px;
          color: #6b7b8d;
          margin-right: 20px;

          &::before {
            margin: 0;
          }
        }

        .project-group-name {
          padding: 2px 20px;
          margin-left: 10px;
          background: #f0f7ff;
          border-radius: 2px;
        }
      }

      .desc {
        margin-top: 5px;
        width: 80%;
        min-height: 20px;
        font-size: 14px;
        color: #6b7b8d;
      }
    }

    .instance-tabs {
      display: flex;
      color: #ffffff;
      white-space: nowrap;

      .tab + .tab {
        margin-left: 8px;
      }

      .tab {
        position: relative;
        min-width: 70px;
        //flex-basis: 70px;
        flex-shrink: 0;
        height: 60px;
        border-radius: 4px 4px 0 0;

        .left-horn,
        .right-horn {
          position: absolute;
          bottom: 0;
          width: 8px;
          height: 8px;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            bottom: 0;
            width: 48px;
            height: 48px;
            overflow: hidden;
            background-color: #fff;
          }
        }

        .left-horn {
          left: -8px;

          &::before {
            right: 0;
            border-bottom-right-radius: 8px;
          }
        }

        .right-horn {
          right: -8px;

          &::before {
            left: 0;
            border-bottom-left-radius: 8px;
          }
        }

        &.init {
          &,
          .left-horn,
          .right-horn {
            background-color: #979797;
          }
        }

        &.running {
          &,
          .left-horn,
          .right-horn {
            background-color: #10c2c2;
          }
        }

        &.finished {
          &,
          .left-horn,
          .right-horn {
            background-color: #3ebb03;
          }
        }

        &.terminated {
          &,
          .left-horn,
          .right-horn {
            background-color: #cf1524;
          }
        }

        &.suspended {
          &,
          .left-horn,
          .right-horn {
            background-color: #7986cb;
          }
        }

        &.unselected {
          cursor: pointer;
          height: 59px;
          border-bottom: 1px solid #fff;
          opacity: 0.55;

          .label {
            padding: 7px 10px 0;
            text-align: right;
            font-size: 20px;
          }
        }

        &.selected {
          cursor: default;

          .label {
            padding-left: 4px;
            line-height: 60px;
            font-size: 26px;
            text-align: center;

            &::before {
              content: '';
              position: absolute;
              left: 0;
              top: 15px;
              width: 4px;
              height: 30px;
              background: rgba(255, 255, 255, 0.8);
              border-radius: 0 100px 100px 0;
              overflow: hidden;
            }
          }
        }
      }
    }

    .instance-tab-content {
      position: relative;
      display: flex;
      padding: 15px 30px;
      color: #ffffff;

      &.init {
        background-color: #979797;
      }

      &.running {
        background-color: #10c2c2;
      }

      &.finished {
        background-color: #3ebb03;
      }

      &.terminated {
        background-color: #cf1524;
      }

      &.suspended {
        background-color: #7986cb;
      }

      .item + .item {
        margin-left: 80px;
      }

      .item {
        font-size: 14px;

        .value {
          font-size: 16px;
          font-weight: bold;
          margin-bottom: 5px;

          &.jm-timer {
            width: 100px;
          }
        }
      }

      .terminate-btn {
        position: absolute;
        top: 14px;
        right: 12px;
        width: 50px;
        height: 50px;
        border-radius: 4px;
        font-size: 36px;
        border: 0;
        background-color: transparent;
        color: #ffffff;
        cursor: pointer;
        outline: none;

        &:active {
          &.running {
            background-color: #55dbdb;
          }

          &.suspended {
            background-color: #8e9ded;
          }
        }
      }
    }
  }

  .workflow-section {
    > :first-child {
      height: calc(100vh - 246px);
    }
  }
}
</style>
