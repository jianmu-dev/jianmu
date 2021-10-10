<template>
  <router-view v-if="childRoute"/>
  <div v-else class="task-definition-manager" v-loading="loadingDefinition">
    <div class="right-top-btn">
      <router-link :to="{name: 'create-task-definition'}">
        <jm-button type="primary" class="jm-icon-button-add" size="small">新增</jm-button>
      </router-link>
    </div>
    <div class="search">
      <jm-input clearable v-model="queryForm.name" placeholder="请输入任务定义名称" suffix-icon="jm-icon-button-search"
                @change="query(true)"/>
    </div>
    <jm-scrollbar class="jm-table-scrollbar">
      <jm-table
        ref="definitionTableRef"
        :data="definitions"
        border
        row-key="ref"
        :expand-row-keys="expandedRowKeys"
        @expand-change="handleRowExpand">
        <jm-table-column type="expand">
          <template #default="{row}">
            <jm-scrollbar class="jm-table-scrollbar" v-loading="loadingDefinitionVersions[definitions.indexOf(row)]">
              <jm-table
                :data="versions[definitions.indexOf(row)]"
                border>
                <jm-table-column
                  fixed
                  label="版本号"
                  align="center"
                  min-width="100"
                  prop="name">
                </jm-table-column>
                <jm-table-column
                  label="描述"
                  align="center"
                  min-width="200"
                  prop="description">
                </jm-table-column>
                <jm-table-column
                  label="创建时间"
                  align="center"
                  min-width="170"
                  prop="createdTime"
                  :formatter="formatDatetime">
                </jm-table-column>
                <jm-table-column
                  label="最后修改时间"
                  align="center"
                  min-width="170"
                  prop="lastModifiedTime"
                  :formatter="formatDatetime">
                </jm-table-column>
                <jm-table-column
                  fixed="right"
                  label="操作"
                  align="center"
                  width="200">
                  <template #default="{row: {taskDefinitionRef, name}}">
                    <div class="operation">
                      <router-link
                        :to="{name: 'task-definition-version-detail', query: {taskDefRef: taskDefinitionRef, taskDefVersion: name}}">
                        <jm-button type="text" size="small">查看</jm-button>
                      </router-link>
                      <router-link
                        :to="{name: 'upgrade-task-definition-version', query: {taskDefRef: taskDefinitionRef, taskDefVersion: name}}">
                        <jm-button type="text" size="small">升级版本</jm-button>
                      </router-link>
                      <jm-button type="text" size="small" @click="deleteVersion(taskDefinitionRef, name)">删除</jm-button>
                    </div>
                  </template>
                </jm-table-column>
              </jm-table>
            </jm-scrollbar>
          </template>
        </jm-table-column>
        <jm-table-column
          label="任务定义名称"
          align="center"
          min-width="200"
          prop="name">
        </jm-table-column>
        <jm-table-column
          label="任务定义Ref"
          align="center"
          min-width="100"
          prop="ref">
        </jm-table-column>
        <jm-table-column
          label="创建时间"
          align="center"
          min-width="170"
          prop="createdTime"
          :formatter="formatDatetime">
        </jm-table-column>
        <jm-table-column
          label="最后修改时间"
          align="center"
          min-width="170"
          prop="lastModifiedTime"
          :formatter="formatDatetime">
        </jm-table-column>
        <jm-table-column
          label="操作"
          align="center"
          width="100">
          <template #default="{row}">
            <jm-button type="text" size="small" @click="definitionTableRef.toggleRowExpansion(row, true)">
              查看版本
            </jm-button>
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
import { defineComponent, getCurrentInstance, onBeforeMount, ref, Ref, toRefs } from 'vue';
import { createNamespacedHelpers, useStore } from 'vuex';
import { onBeforeRouteUpdate, RouteLocationNormalized, RouteLocationNormalizedLoaded, useRoute } from 'vue-router';
import { namespace } from '@/store/modules/task-definition';
import { IDeleteVersionForm, IQueryForm, IState } from '@/model/modules/task-definition';
import { ITaskDefinitionVo } from '@/api/dto/task-definition';
import { DEFAULT_PAGE_SIZE, START_PAGE_NUM } from '@/utils/constants';
import { datetimeFormatter } from '@/utils/formatter';
import { calculateTotalPages } from '@/utils/pagination';
import { namespace as sessionNs } from '@/store/modules/session';
import { IState as ISessionState } from '@/model/modules/session';

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
    const queryForm = ref<IQueryForm>({
      name: '',
      pageNum: START_PAGE_NUM,
      pageSize: pageSize ? (pageSize[namespace] || DEFAULT_PAGE_SIZE) : DEFAULT_PAGE_SIZE,
    });
    const definitionTableRef = ref<any>(null);
    const expandedRowKeys = ref<string[]>([]);
    const loadingDefinition = ref<boolean>(false);
    const loadingDefinitionVersions = ref<boolean[]>([]);

    const loadDefinition = async () => {
      loadingDefinition.value = !loadingDefinition.value;
      await proxy.queryDefinition({ ...queryForm.value });
      loadingDefinition.value = !loadingDefinition.value;
    };

    // 初始化任务定义列表
    onBeforeMount(() => loadDefinition());

    const childRoute = ref<boolean>(false);
    changeView(childRoute, useRoute());
    onBeforeRouteUpdate(to => changeView(childRoute, to));

    return {
      ...toRefs(state),
      childRoute,
      queryForm,
      definitionTableRef,
      expandedRowKeys,
      loadingDefinition,
      loadingDefinitionVersions,
      ...mapSessionMutations({
        mutationPageSize: 'mutationPageSize',
      }),
      ...mapActions({
        queryDefinition: 'query',
        listDefinitionVersion: 'listVersion',
        deleteDefinitionVersion: 'deleteVersion',
      }),
      formatDatetime: (row: any, column: any) => {
        return datetimeFormatter(row[column.property]);
      },
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
      handleRowExpand: async (row: ITaskDefinitionVo, expandedRows: ITaskDefinitionVo[]) => {
        if (!expandedRows.includes(row)) {
          // 关闭场景
          expandedRowKeys.value = [];
          return;
        }

        // 展开场景
        const index = state.definitions.indexOf(row);
        loadingDefinitionVersions.value[index] = !loadingDefinitionVersions.value[index];
        await proxy.listDefinitionVersion(row.ref);
        loadingDefinitionVersions.value[index] = !loadingDefinitionVersions.value[index];

        expandedRowKeys.value = [row.ref];
      },

      deleteVersion(taskDefRef: string, taskDefVersion: string) {
        proxy.$confirm('确定要删除任务定义版本吗?', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(() => {
          proxy.deleteDefinitionVersion({ taskDefRef, taskDefVersion } as IDeleteVersionForm).then(() => {
            proxy.$success('删除成功');

            const index = state.definitions.findIndex(item => item.ref === taskDefRef);
            if (state.versions[index].length === 0) {
              // 刷新任务定义列表，保留查询、分页等状态
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
.task-definition-manager {
  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-add::before {
      font-weight: bold;
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
