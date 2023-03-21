<template>
  <div class="pipeline" v-loading="loading">
    <jm-workflow-editor v-model="workflow" @back="close" @save="save" v-if="loaded" />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, inject, nextTick, onMounted, ref } from 'vue';
import { IWorkflow } from '@/components/workflow/workflow-editor/model/data/common';
import { useRoute, useRouter } from 'vue-router';
import { save as saveProject } from '@/api/project';
import { fetchProjectDetail } from '@/api/view-no-auth';
import yaml from 'yaml';
import { Global } from '@/components/workflow/workflow-editor/model/data/global';
import { IGitRepoBranchVo } from '@/api/dto/git-repo';
import { getBranches } from '@/api/git-repo';
import { toEntry } from '@/utils/jump-address';
import sleep from '@/utils/sleep';

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
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const route = useRoute();
    const entry = computed<boolean>(() => true);
    const { payload } = route.params;
    const loading = ref<boolean>(false);
    // workflow数据是否加载完成
    const loaded = ref<boolean>(false);
    const reloadMain = inject('reloadMain') as () => void;
    const editMode = !!props.id;
    const workflow = ref<IWorkflow>();
    // 项目分支信息
    const branches = ref<IGitRepoBranchVo[]>([]);
    onMounted(async () => {
      // 如果路由中带有workflow的回显数据不在发送请求
      if (payload && editMode) {
        const { projectGroupId, branch, dslText } = JSON.parse(payload as string);
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
          global: {
            concurrent: global ? global.concurrent : 1,
            caches: global.cache ? global.cache : undefined,
            params: global.param ? global.param : undefined,
          },
          data: rawData,
        };
        loaded.value = false;
        await nextTick();
        loaded.value = true;
        return;
      }
      if (editMode) {
        try {
          loading.value = true;
          loaded.value = false;
          const { dslText: t, projectGroupId: id, branch: b } = await fetchProjectDetail(props.id as string);
          const dslData = yaml.parse(t);
          const rawDslData = dslData['raw-data'];
          const { name: n, global: g, description: d } = dslData;
          workflow.value = {
            name: n,
            groupId: id,
            description: d,
            association: {
              entry: entry.value,
              branch: b,
            },
            global: {
              concurrent: g ? g.concurrent : 1,
              caches: g.cache ? g.cache : undefined,
              params: g.param ? g.param : undefined,
            },
            data: rawDslData,
          };
        } catch (err) {
          proxy.$throw(err, proxy);
        } finally {
          loading.value = false;
          loaded.value = true;
        }
      } else {
        let branchData = props.branch;
        // 获取分支信息（如果entry为true时，有必要获取分支信息）
        if (entry.value) {
          branches.value = await getBranches();
          const flag = branches.value.some(item => {
            return item.branchName === branchData;
          });
          if (!flag) {
            branchData = branches.value.find(item => item.isDefault)!.branchName;
          }
        }
        workflow.value = {
          name: '未命名项目',
          groupId: '1',
          description: '',
          association: {
            entry: entry.value,
            branch: branchData,
          },
          global: {
            concurrent: 1,
          },
          data: '',
        };
        loaded.value = true;
      }
    });
    const close = async () => {
      if (!entry.value) {
        await router.push({ name: 'index' });
        return;
      }
      toEntry();
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
          await sleep(500);
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
