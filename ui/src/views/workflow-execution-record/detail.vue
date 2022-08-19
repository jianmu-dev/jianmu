<template>
  <div class="record-detail" v-loading="loading">
    <jm-workflow-detail
      v-model="workflowDetail"
      :session="session"
      :entry="entry"
      @jump="handleJump"
      @back="handleBack"
      @logout="handleLogout"
      @trigger="handleTrigger"
      @update:model-value="handleChangeRouterParam"
    />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, PropType, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { namespace as sessionNs } from '@/store/modules/session';
import { IState } from '@/model/modules/session';
import { ISessionVo } from '@/api/dto/session';
import { IWorkflowDetailParam } from '@/components/workflow/workflow-detail/model/data/common';
import { ViewModeEnum } from '@/api/dto/enumeration';
// import { LOGIN_INDEX } from '@/router/path-def';
import { IRootState } from '@/model';

export default defineComponent({
  props: {
    projectId: {
      type: String,
      required: true,
    },
    viewMode: {
      type: String as PropType<ViewModeEnum>,
      default: ViewModeEnum.GRAPHIC,
    },
    triggerId: String,
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const store = useStore();
    const state = store.state[sessionNs] as IState;
    const rootState = store.state as IRootState;
    const entry = rootState.entry || false;
    const entryUrl = store.state[sessionNs].session.entryUrl;
    const loading = ref<boolean>(false);
    // workflow 详情数据是否加载完成
    const loaded = ref<boolean>(false);
    const workflowDetail = ref<IWorkflowDetailParam>({
      projectId: props.projectId,
      viewMode: props.viewMode,
      triggerId: props.triggerId,
    });
    return {
      entry,
      loading,
      loaded,
      workflowDetail,
      session: computed<ISessionVo | undefined>(() => state.session),
      handleBack() {
        if (!entry) {
          // router.push({ name: 'index' });
          const { fullPath } = rootState.fromRoute;
          router.push(fullPath);
          return;
        }
        window.location.href = entryUrl;
      },
      handleJump(projectGroupId: string) {
        if (!projectGroupId) {
          return;
        }
        router.push({ path: `/project-group/detail/${projectGroupId}` });
      },
      handleLogout() {
        console.log('退出登录事件');
        // try {
        //   // 清理token
        //   proxy.deleteSession();
        //   proxy.$success('退出成功');

        //   router.push(LOGIN_INDEX);
        // } catch (err) {
        //   proxy.$throw(err, proxy);
        // }
      },
      handleTrigger(err?: Error) {
        if (!err) {
          proxy.$success('操作成功');
          router.push({
            name: 'workflow-execution-record-detail',
            query: {
              projectId: props.projectId,
            },
          });
        } else {
          proxy.$throw(err, proxy);
        }
      },
      handleChangeRouterParam(routerParam: { viewMode?: ViewModeEnum, triggerId?: string }) {
        if (!routerParam.triggerId) {
          delete routerParam.triggerId;
        }
        router.push({
          name: 'workflow-execution-record-detail',
          query: {
            ...routerParam,
          },
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.record-detail {
  height: 100vh;
  position: relative;
}
</style>
