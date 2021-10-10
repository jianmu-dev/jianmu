<template>
  <router-view v-if="childRoute"/>
  <div v-show="!childRoute" class="workflow-execution-record-manager">
    <div class="refresh">
      <jm-button type="text" class="jm-icon-button-refresh" size="small" :loading="refreshLoading" @click="refresh">刷新
      </jm-button>
    </div>
    <jm-tabs v-model="activatedTab">
      <jm-tab-pane name="executing">
        <template #label>
          <jm-badge :value="totalElements.executing" class="tab-title">
            <i class="jm-icon-tab-executing"></i> 执行中
          </jm-badge>
        </template>
        <div class="tab-content">
          <workflow-execution-record-list :type="'executing'" :refreshing="refreshing.value"
                                          @refreshed="handleRefreshed"/>
        </div>
      </jm-tab-pane>
      <jm-tab-pane name="completed">
        <template #label>
          <jm-badge :value="totalElements.completed" class="tab-title">
            <i class="jm-icon-tab-complete"></i> 执行完成
          </jm-badge>
        </template>
        <div class="tab-content">
          <workflow-execution-record-list :type="'completed'" :refreshing="refreshing.value"
                                          @refreshed="handleRefreshed"/>
        </div>
      </jm-tab-pane>
    </jm-tabs>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, provide, ref, Ref, toRefs } from 'vue';
import { useStore } from 'vuex';
import { onBeforeRouteUpdate, RouteLocationNormalized, RouteLocationNormalizedLoaded, useRoute } from 'vue-router';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import WorkflowExecutionRecordList from './list.vue';

function changeView(childRoute: Ref<boolean>, route: RouteLocationNormalizedLoaded | RouteLocationNormalized) {
  childRoute.value = route.matched.length > 2;
}

export default defineComponent({
  components: {
    WorkflowExecutionRecordList,
  },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const refreshing = ref<{
      value: boolean;
      completedTabs: {
        [key: string]: boolean;
      };
    }>({
      value: false,
      completedTabs: {
        executing: true,
        completed: true,
      },
    });

    const childRoute = ref<boolean>(false);
    changeView(childRoute, useRoute());
    onBeforeRouteUpdate(to => changeView(childRoute, to));

    const refresh = () => {
      refreshing.value = {
        value: true,
        completedTabs: {
          executing: false,
          completed: false,
        },
      };

      proxy.$nextTick(() => (refreshing.value.value = false));
    };

    provide('refreshRecords', refresh);

    return {
      ...toRefs(state),
      refreshing,
      childRoute,
      activatedTab: ref<string>('executing'),
      refresh,
      handleRefreshed: (type: string) => {
        refreshing.value.completedTabs[type] = true;
      },
      refreshLoading: computed<boolean>(() => {
        for (const item of Object.values(refreshing.value.completedTabs)) {
          if (!item) {
            return true;
          }
        }

        return false;
      }),
    };
  },
});
</script>

<style scoped lang="less">
.workflow-execution-record-manager {
  position: relative;

  .refresh {
    position: absolute;
    right: 12px;
    top: 19px;
    z-index: 2;
  }

  ::v-deep(.el-tabs) {
    .el-tabs__item {
      &:not([class*=is-active]) {
        color: #606266;
      }

      &:hover {
        color: #0091FF;
      }
    }

    .el-tabs__header {
      background-color: #FFFFFF;
    }
  }

  .tab-title {
    padding-right: 30px;

    ::v-deep(.el-badge__content) {
      color: inherit;
      border-color: transparent;

      &.is-fixed {
        top: 20px;
        right: 20px;
      }

      &.el-badge__content--primary {
        background-color: rgba(140, 140, 140, 0.1);
      }
    }
  }

  .is-active .tab-title, .tab-title:hover {
    ::v-deep(.el-badge__content.el-badge__content--primary) {
      background-color: #E6F7FF;
    }
  }

  .tab-content {

  }
}
</style>
