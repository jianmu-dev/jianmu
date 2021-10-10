<template>
  <router-view v-if="childRoute"/>
  <div v-else class="workflow-definition-manager" v-loading="loading">
    <div class="right-top-btn">
      <router-link :to="{name: 'import-workflow-definition'}">
        <jm-button type="primary" class="jm-icon-button-download" size="small">导入</jm-button>
      </router-link>
      <router-link :to="{name: 'create-workflow-definition'}">
        <jm-button type="primary" class="jm-icon-button-add" size="small">新增</jm-button>
      </router-link>
    </div>

    <div class="search">
      <jm-input clearable v-model="queryForm.name" placeholder="请输入流程名称" suffix-icon="jm-icon-button-search"
                @change="query(true)"/>
    </div>
    <jm-scrollbar class="jm-table-scrollbar">
      <jm-table
        :data="definitions"
        border>
        <jm-table-column
          fixed
          label="流程名称"
          align="center"
          min-width="200"
          prop="workflowName">
        </jm-table-column>
        <jm-table-column
          label="流程ref"
          align="center"
          min-width="100"
          prop="workflowRef">
        </jm-table-column>
        <jm-table-column
          label="步骤"
          min-width="60"
          align="center"
          prop="steps">
        </jm-table-column>
        <jm-table-column
          label="创建时间"
          align="center"
          min-width="170"
          prop="createdTime"
          class-name="time"
          :formatter="formatDatetime">
        </jm-table-column>
        <jm-table-column
          label="最后修改时间"
          align="center"
          min-width="170"
          prop="lastModifiedTime"
          class-name="time"
          :formatter="formatDatetime">
        </jm-table-column>
        <jm-table-column
          fixed="right"
          label="操作"
          align="center"
          width="200">
          <template #default="{row: { id, dslSource, workflowRef, workflowVersion }}">
            <div class="operation">
              <router-link :to="{name: 'workflow-definition-detail', query: { workflowRef, workflowVersion }}">
                <jm-button type="text" size="small">查看</jm-button>
              </router-link>
              <router-link v-if="dslSource === DslSourceEnum.LOCAL"
                           :to="{name: 'update-workflow-definition', params: { id }}">
                <jm-button type="text" size="small">编辑</jm-button>
              </router-link>
              <jm-button v-else-if="dslSource === DslSourceEnum.GIT" type="text" size="small" @click="sync(id)">同步
              </jm-button>
              <jm-button type="text" size="small" @click="execute(id)">立即执行</jm-button>
              <jm-button type="text" size="small" @click="del(id)">删除</jm-button>
            </div>
          </template>
        </jm-table-column>
      </jm-table>
    </jm-scrollbar>
    <div class="pagination">
      <jm-pagination v-model:current-page="queryForm.pageNum" :total="totalElements"
                     v-model:page-size="queryForm.pageSize"
                     @current-change="query(false)" @size-change="changePageSize"/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, inject, onBeforeMount, ref, Ref, toRefs } from 'vue';
import { onBeforeRouteUpdate, RouteLocationNormalized, RouteLocationNormalizedLoaded, useRoute } from 'vue-router';
import { createNamespacedHelpers, useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-definition';
import { IState } from '@/model/modules/workflow-definition';
import { IQueryForm } from '@/model/modules/task-definition';
import { DEFAULT_PAGE_SIZE, START_PAGE_NUM } from '@/utils/constants';
import { del, executeImmediately, synchronize } from '@/api/workflow-definition';
import { datetimeFormatter } from '@/utils/formatter';
import { calculateTotalPages } from '@/utils/pagination';
import { namespace as sessionNs } from '@/store/modules/session';
import { IState as ISessionState } from '@/model/modules/session';
import { DslSourceEnum } from '@/api/dto/enumeration';

const { mapMutations: mapSessionMutations } = createNamespacedHelpers(sessionNs);
const { mapActions } = createNamespacedHelpers(namespace);

function changeView(childRoute: Ref<boolean>, route: RouteLocationNormalizedLoaded | RouteLocationNormalized) {
  childRoute.value = route.matched.length > 2;
}

export default defineComponent({
  setup() {
    const { userSettings: { pageSize } } = useStore().state[sessionNs] as ISessionState;
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const reloadMain = inject('reloadMain') as () => void;
    const queryForm = ref<IQueryForm>({
      name: '',
      pageNum: START_PAGE_NUM,
      pageSize: pageSize ? (pageSize[namespace] || DEFAULT_PAGE_SIZE) : DEFAULT_PAGE_SIZE,
    });
    const loading = ref<boolean>(false);

    const loadDefinition = async () => {
      loading.value = !loading.value;
      await proxy.queryDefinition({ ...queryForm.value });
      loading.value = !loading.value;
    };

    // 初始化流程定义列表
    onBeforeMount(() => loadDefinition());

    const childRoute = ref<boolean>(false);
    changeView(childRoute, useRoute());
    onBeforeRouteUpdate((to, { name }) => {
      if (name === 'update-workflow-definition') {
        // 从编辑流程定义页面返回时，刷新列表
        // 因为编辑导致流程定义id变化
        loadDefinition();
      }

      changeView(childRoute, to);
    });

    return {
      ...toRefs(state),
      childRoute,
      queryForm,
      loading,
      DslSourceEnum,
      ...mapSessionMutations({
        mutationPageSize: 'mutationPageSize',
      }),
      ...mapActions({
        queryDefinition: 'query',
      }),
      query: (resetPageNum: boolean) => {
        if (resetPageNum) {
          queryForm.value.pageNum = START_PAGE_NUM;
        }

        loadDefinition();
      },
      changePageSize: () => {
        const totalPages = calculateTotalPages(state.totalElements, queryForm.value.pageSize);

        if (queryForm.value.pageNum <= totalPages) {
          // 不触发当前页码的改变，强制刷新
          proxy.query(false);
        }

        proxy.mutationPageSize({
          key: namespace,
          value: queryForm.value.pageSize,
        });
      },
      formatDatetime: (row: any, column: any) => {
        return datetimeFormatter(row[column.property]);
      },
      sync: (id: string) => {
        proxy.$confirm('确定要同步吗?', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        }).then(() => {
          synchronize(id).then(() => {
            proxy.$success('同步成功');

            // 刷新流程定义列表，保留查询、分页等状态
            loadDefinition();
          }).catch((err: Error) => proxy.$throw(err, proxy));
        }).catch(() => {
        });
      },
      execute: (id: string) => {
        proxy.$confirm('确定要执行吗?', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        }).then(() => {
          executeImmediately(id).then(() => {
            proxy.$success('操作成功');
          }).catch((err: Error) => proxy.$throw(err, proxy));
        }).catch(() => {
        });
      },
      del: (id: string) => {
        proxy.$confirm('确定要删除流程定义吗?', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(() => {
          del(id).then(() => {
            proxy.$success('删除成功');

            if (state.definitions.length === 1) {
              reloadMain();
            } else {
              // 刷新流程定义列表，保留查询、分页等状态
              loadDefinition();
            }
          }).catch((err: Error) => proxy.$throw(err, proxy));
        }).catch(() => {
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.workflow-definition-manager {
  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-download::before,
    .jm-icon-button-add::before {
      font-weight: bold;
    }

    > :nth-child(n+2) {
      margin-left: 10px;
    }
  }

  .search {
    margin-bottom: 16px;
    padding-left: 24px;
    height: 68px;
    background-color: #FFFFFF;
    border-radius: 4px;
    border: 1px solid #E6EBF2;
    display: flex;
    align-items: center;

    .el-input {
      width: 444px;
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
