<template>
  <div class="record-detail" v-loading="loading">
    <jm-workflow-detail
      v-model="workflowDetail"
      :session="session"
      :entry="entry"
      @jump="handleJump"
      @back="toEntry"
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
import { ISession } from '@/model/modules/session';
import { IWorkflowDetailParam } from '@/components/workflow/workflow-detail/model/data/common';
import { ViewModeEnum } from '@/api/dto/enumeration';
import { toEntry } from '@/utils/jump-address';

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
    redirectUrl: String,
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const store = useStore();
    const state = store.state[sessionNs] as IState;
    const entry = true;

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
      session: computed<ISession | undefined>(() => state.session),
      toEntry,
      handleJump(projectGroupId: string) {
        if (!projectGroupId) {
          return;
        }
        router.push({ path: `/project-group/detail/${projectGroupId}` });
      },
      handleLogout() {
        console.log('退出登录事件');
        // TODO login 退出登录
      },
      handleTrigger(msg: string, err?: Error) {
        if (!err) {
          proxy.$success(msg);
          // 清空当前选中record
          workflowDetail.value.triggerId = '';
          // 地址跳转
          router.replace({
            name: 'workflow-execution-record-detail',
            query: {
              projectId: props.projectId,
              redirectUrl: props.redirectUrl,
            },
          });
        } else {
          proxy.$throw(err, proxy);
        }
      },
      handleChangeRouterParam(routerParam: { viewMode?: ViewModeEnum; triggerId?: string }) {
        if (!routerParam.triggerId) {
          delete routerParam.triggerId;
        }
        const query = {
          ...routerParam,
        };
        if (props.redirectUrl) {
          Object.assign(query, {
            redirectUrl: props.redirectUrl,
          });
        }
        router.replace({
          name: 'workflow-execution-record-detail',
          query,
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
