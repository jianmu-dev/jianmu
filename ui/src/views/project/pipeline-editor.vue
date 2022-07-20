<template>
  <div class="pipeline" v-loading="loading">
    <jm-workflow-editor v-model="workflow" @back="close" @save="save"
                        v-if="loaded"/>
  </div>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  getCurrentInstance,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
} from 'vue';
import { IWorkflow } from '@/components/workflow/workflow-editor/model/data/common';
import { useRoute, useRouter } from 'vue-router';
import { save as saveProject } from '@/api/project';
import { fetchProjectDetail } from '@/api/view-no-auth';
import yaml from 'yaml';
import { namespace } from '@/store/modules/session';
import { createNamespacedHelpers, useStore } from 'vuex';
import dynamicRender from '@/utils/dynamic-render';
import LoginVerify from '@/views/login/dialog.vue';
import { ISessionVo } from '@/api/dto/session';
import { Global } from '@/components/workflow/workflow-editor/model/data/global';
import { IGitRepoBranchVo } from '@/api/dto/git-repo';
import { getBranches } from '@/api/git-repo';

const { mapMutations } = createNamespacedHelpers(namespace);
export default defineComponent({
  props: {
    id: {
      type: String,
    },
    branch: {
      type: String,
    },
  },
  setup(props) {
    const { proxy, appContext } = getCurrentInstance() as any;
    const router = useRouter();
    const route = useRoute();
    const store = useStore();
    const entry = computed<boolean>(() => store.state.entry);
    const sessionState = { ...store.state[namespace] };
    const entryUrl = sessionState.session.entryUrl;
    const { payload } = route.params;
    const loading = ref<boolean>(false);
    // workflow数据是否加载完成
    const loaded = ref<boolean>(false);
    const reloadMain = inject('reloadMain') as () => void;
    const editMode = !!props.id;
    const workflow = ref<IWorkflow>();
    const defaultSession = ref<ISessionVo>();
    // 项目分支信息
    const branches = ref<IGitRepoBranchVo[]>([]);
    const refreshState = (e: any) => {
      if (e.key !== 'session') {
        return;
      }
      defaultSession.value = JSON.parse(e.newValue)['_default'].session;
      // 将新tab中的session值，保存在vuex中
      proxy.mutateSession(defaultSession.value);
    };
    // 验证用户是否登录
    const authLogin = () => {
      if (sessionState.session) {
        return;
      }
      dynamicRender(LoginVerify, appContext);
    };
    onMounted(async () => {
      window.addEventListener('storage', refreshState);
      // 如果路由中带有workflow的回显数据不在发送请求
      if (payload && editMode) {
        const { name, global, description, data, groupId, branch } = JSON.parse(payload as string);
        workflow.value = {
          name,
          groupId,
          description,
          association: {
            entry: entry.value,
            branch,
          },
          global: new Global(global?.concurrent, global?.params),
          data,
        };
        loaded.value = false;
        await nextTick();
        loaded.value = true;
        return;
      }
      if (editMode) {
        authLogin();
        try {
          loading.value = true;
          loaded.value = false;
          const { dslText, projectGroupId, branch } = await fetchProjectDetail(props.id as string);
          const dsl = yaml.parse(dslText);
          const rawData = dsl['raw-data'];
          const { name, global, description } = dsl;
          workflow.value = {
            name,
            groupId: projectGroupId,
            description,
            association: {
              entry: entry.value,
              branch,
            },
            global: new Global(global?.concurrent, global?.params),
            data: rawData,
          };
        } catch (err) {
          proxy.$throw(err, proxy);
        } finally {
          loading.value = false;
          loaded.value = true;
        }
      } else {
        authLogin();
        let branch = props.branch;
        // 获取分支信息（如果entry为true时，有必要获取分支信息）
        if (entry.value) {
          branches.value = await getBranches();
          const flag = branches.value.some(item => {
            return item.branchName === branch;
          });
          if (!flag) {
            branch = branches.value.find(item => item.isDefault).branchName;
          }
        }
        workflow.value = {
          name: '未命名项目',
          groupId: '1',
          description: '',
          association: {
            entry: entry.value,
            branch,
          },
          global: new Global(),
          data: '',
        };
        loaded.value = true;
      }
    });
    onBeforeUnmount(() => {
      window.removeEventListener('storage', refreshState);
    });
    const close = async () => {
      if (!entry.value) {
        await router.push({ name: 'index' });
        return;
      }
      window.location.href = entryUrl;
    };
    return {
      loaded,
      loading,
      workflow,
      close,
      save: async (back: boolean, dsl: string) => {
        try {
          const payload = {
            projectGroupId: workflow.value!.groupId,
            dslText: dsl,
            id: editMode ? props.id : '',
            branch: workflow.value!.association.branch,
          };
          entry.value ? Reflect.deleteProperty(payload, 'projectGroupId') : Reflect.deleteProperty(payload, 'branch');
          const { id } = await saveProject(payload);
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
