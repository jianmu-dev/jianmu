<template>
  <div class="pipeline" v-loading="loading">
    <jm-workflow-editor v-model="workflow" @back="close" @save="save" v-if="!loaded" />
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, inject, nextTick, onMounted, ref } from 'vue';
import { IWorkflow } from '@/components/workflow/workflow-editor/model/data/common';
import { useRoute, useRouter } from 'vue-router';
import { save as saveProject } from '@/api/project';
import { fetchProjectDetail } from '@/api/view-no-auth';
import yaml from 'yaml';
import { namespace } from '@/store/modules/session';
import { createNamespacedHelpers, useStore } from 'vuex';
import LoginVerify from '@/views/login/dialog.vue';

const { mapMutations, mapActions } = createNamespacedHelpers(namespace);
export default defineComponent({
  props: {
    id: {
      type: String,
    },
  },
  setup(props) {
    const { proxy, appContext } = getCurrentInstance() as any;
    const router = useRouter();
    const route = useRoute();
    const store = useStore();
    const sessionState = { ...store.state[namespace] };
    const { payload } = route.params;
    const loading = ref<boolean>(false);
    // workflow数据是否加载完成
    const loaded = ref<boolean>(false);
    const reloadMain = inject('reloadMain') as () => void;
    const editMode = !!props.id;
    const workflow = ref<IWorkflow>({
      name: '未命名项目',
      groupId: '1',
      description: '',
      global: {
        concurrent: 1,
      },
      data: '',
    });
    // 验证用户是否登录
    const authLogin = () => {
      if (sessionState.session) {
        return;
      }
      proxy.openAuthDialog({ appContext, LoginVerify });
    };
    onMounted(async () => {
      if (payload && editMode) {
        // 初始化走这里获取到的cache为空
        workflow.value = JSON.parse(payload as string);
        loaded.value = true;
        await nextTick();
        loaded.value = false;
        return;
      }
      if (editMode) {
        authLogin();
        try {
          loading.value = true;
          loaded.value = true;
          const { dslText, projectGroupId } = await fetchProjectDetail(props.id as string);
          const rawData = yaml.parse(dslText)['raw-data'];
          const { name, global, description } = yaml.parse(dslText);
          workflow.value = {
            name,
            groupId: projectGroupId,
            description,
            global: {
              concurrent: global ? global.concurrent : 1,
              caches: global.cache ? global.cache : undefined,
            },
            data: rawData,
          };
        } catch (err) {
          proxy.$throw(err, proxy);
        } finally {
          loading.value = false;
          loaded.value = false;
        }
      } else {
        authLogin();
      }
    });
    const close = async () => {
      await router.push({ name: 'index' });
    };
    return {
      loaded,
      loading,
      workflow,
      close,
      save: async (back: boolean, dsl: string) => {
        try {
          const { id } = await saveProject({
            projectGroupId: workflow.value.groupId,
            dslText: dsl,
            id: editMode ? props.id : '',
          });
          proxy.$success(editMode ? '保存成功' : '新增成功');
          if (!back) {
            // 新增项目，再次点击保存进入项目编辑模式
            if (!editMode) {
              await router.push({ name: 'update-pipeline', params: { id } });
              reloadMain();
              return;
            }
            return;
          }
          await close();
        } catch (err) {
          proxy.$throw(err, proxy);
        }
      },
      ...mapMutations({
        mutateSession: 'oauthMutate',
      }),
      ...mapActions({
        openAuthDialog: 'openAuthDialog',
      }),
    };
  },
});
</script>

<style scoped lang="less">
.pipeline {
  height: 100vh;
  position: relative;
}
</style>
