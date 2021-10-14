<template>
  <div class="workflow-execution-record-list" v-loading="loading">
    <div class="search">
      <jm-input clearable v-model="queryForm.name" placeholder="请输入流程名称"
                suffix-icon="jm-icon-button-search" @change="query(true)"/>
      <jm-input clearable v-model="queryForm.workflowVersion" placeholder="请输入流程版本"
                suffix-icon="jm-icon-button-search" @change="query(true)"/>
      <jm-input clearable v-model="queryForm.id" placeholder="请输入流程实例ID"
                suffix-icon="jm-icon-button-search" @change="query(true)"/>
    </div>
    <jm-scrollbar class="jm-table-scrollbar">
      <jm-table
        :data="data.list"
        border>
        <jm-table-column
          fixed
          label="流程名称"
          align="center"
          min-width="200"
          prop="name">
        </jm-table-column>
        <jm-table-column
          label="流程版本"
          align="center"
          min-width="100"
          prop="workflowVersion">
        </jm-table-column>
        <jm-table-column
          label="启动时间"
          align="center"
          min-width="170"
          prop="startTime"
          :formatter="formatDatetime">
        </jm-table-column>
        <jm-table-column
          v-if="type === 'completed'"
          label="结束时间"
          align="center"
          min-width="170"
          prop="endTime"
          :formatter="formatDatetime">
        </jm-table-column>
        <jm-table-column
          label="执行时长"
          align="center"
          min-width="210"
          :formatter="formatExecutionTime">
        </jm-table-column>
        <jm-table-column
          label="最后执行任务状态"
          align="center"
          min-width="140"
          prop="latestTaskStatus">
          <template #default="{row: {latestTaskStatus}}">
            <task-state :status="latestTaskStatus"/>
          </template>
        </jm-table-column>
        <jm-table-column
          label="流程实例ID"
          align="center"
          min-width="120"
          prop="id">
        </jm-table-column>
        <jm-table-column
          fixed="right"
          label="操作"
          align="center"
          :width="executing ? 120 : 60">
          <template #default="{row: {id}}">
            <div class="operation">
              <router-link :to="{name: 'workflow-execution-record-detail', params: {id}}">
                <jm-button type="text" size="small">查看</jm-button>
              </router-link>
              <jm-button type="text" size="small" v-if="executing" @click="terminate(id)">终止</jm-button>
            </div>
          </template>
        </jm-table-column>
      </jm-table>
    </jm-scrollbar>
    <div class="pagination">
      <jm-pagination v-model:current-page="queryForm.pageNum" :total="data.total"
                     v-model:page-size="queryForm.pageSize"
                     @current-change="query(false)" @size-change="changePageSize"/>
    </div>
  </div>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  getCurrentInstance,
  inject,
  onBeforeMount,
  ref,
  SetupContext,
  watchEffect,
} from 'vue';
import { createNamespacedHelpers, useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IQueryForm, IState } from '@/model/modules/workflow-execution-record';
import { DEFAULT_PAGE_SIZE, START_PAGE_NUM } from '@/utils/constants';
import { WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import TaskState from '@/views/workflow-execution-record/task-state.vue';
import { datetimeFormatter, executionTimeFormatter } from '@/utils/formatter';
import { calculateTotalPages } from '@/utils/pagination';
import { namespace as sessionNs } from '@/store/modules/session';
import { IState as ISessionState } from '@/model/modules/session';
import { terminate } from '@/api/workflow-execution-record';

const { mapMutations: mapSessionMutations } = createNamespacedHelpers(sessionNs);
const { mapActions } = createNamespacedHelpers(namespace);

export default defineComponent({
  components: { TaskState },
  props: {
    type: {
      type: String,
      required: true,
      validator: value => ['executing', 'completed'].includes(value as string),
    },
    refreshing: Boolean,
  },
  emits: ['refreshed'],
  setup(props: any, { emit }: SetupContext) {
    const { userSettings: { pageSize } } = useStore().state[sessionNs] as ISessionState;
    const PAGE_SIZE_KEY = `${namespace}_${props.type}`;
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const executing = ref<boolean>(props.type === 'executing');
    const data = computed(() => executing.value ? state.executing : state.completed);
    const queryForm = ref<IQueryForm>({
      id: '',
      name: '',
      workflowVersion: '',
      status: executing.value ? WorkflowExecutionRecordStatusEnum.RUNNING : WorkflowExecutionRecordStatusEnum.FINISHED,
      pageNum: START_PAGE_NUM,
      pageSize: pageSize ? (pageSize[PAGE_SIZE_KEY] || DEFAULT_PAGE_SIZE) : DEFAULT_PAGE_SIZE,
    });
    const loading = ref<boolean>(false);

    const loadRecord = async () => {
      loading.value = !loading.value;
      await proxy.queryRecord({ ...queryForm.value });
      loading.value = !loading.value;
    };

    // 初始化流程执行记录列表
    onBeforeMount(() => loadRecord());

    watchEffect(async () => {
      if (props.refreshing) {
        await loadRecord();

        const { id, name, workflowVersion, status } = queryForm.value;

        if (id || name || workflowVersion) {
          // 有查询时，刷新总数量
          await proxy.fetchTotalElement(status);
        }

        emit('refreshed', proxy.type);
      }
    });

    const refreshRecords = inject('refreshRecords') as () => void;

    return {
      executing,
      data,
      queryForm,
      loading,
      ...mapSessionMutations({
        mutationPageSize: 'mutationPageSize',
      }),
      ...mapActions({
        fetchTotalElement: 'fetchTotalElement',
        queryRecord: 'query',
      }),
      formatDatetime: (row: any, column: any) => {
        return datetimeFormatter(row[column.property]);
      },
      formatExecutionTime: ({ startTime, endTime, status }: any) => {
        return executionTimeFormatter(startTime, endTime, status === WorkflowExecutionRecordStatusEnum.RUNNING);
      },
      query: (resetPageNum: boolean) => {
        if (resetPageNum) {
          queryForm.value.pageNum = START_PAGE_NUM;
        }

        loadRecord();
      },
      changePageSize: () => {
        const totalPages = calculateTotalPages(data.value.total, queryForm.value.pageSize);

        if (queryForm.value.pageNum <= totalPages) {
          // 不触发当前页码的改变，强制刷新
          proxy.query(false);
        }

        proxy.mutationPageSize({
          key: PAGE_SIZE_KEY,
          value: queryForm.value.pageSize,
        });
      },
      terminate: (id: string) => {
        proxy.$confirm('确定要终止吗?', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        }).then(() => {
          terminate(id).then(() => {
            proxy.$success('终止成功');

            // 刷新流程执行记录列表，保留查询、分页等状态
            refreshRecords();
          }).catch((err: Error) => proxy.$throw(err, proxy));
        }).catch(() => {
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.workflow-execution-record-list {
  background-color: #FFFFFF;
  overflow: auto;

  .search {
    padding: 24px 24px 0;
    display: flex;
    flex-wrap: wrap;
    align-items: center;

    .el-input {
      width: 330px;
      margin-bottom: 16px;
      margin-right: 24px;
    }
  }

  .operation {
    > :nth-child(n+2) {
      margin-left: 10px;
    }
  }

  .pagination {
    margin-bottom: 16px;
    background-color: #FFFFFF;
    text-align: right;
    padding: 10px 20px;
    border-left: 1px solid #EBEEF5;
    border-right: 1px solid #EBEEF5;
    border-bottom: 1px solid #EBEEF5;
    overflow: auto;
  }
}
</style>