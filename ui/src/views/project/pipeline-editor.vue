<template>
  <div class="pipeline" v-loading="loading">
    <jm-workflow-editor v-model="workflow" @back="close" @save="save"
                        v-if="!loading"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, inject, onMounted, ref } from 'vue';
import { IWorkflow } from '@/components/workflow/workflow-editor/model/data/common';
import { useRouter } from 'vue-router';
import { save as saveProject } from '@/api/project';
import { fetchProjectDetail } from '@/api/view-no-auth';
import yaml from 'yaml';

export default defineComponent({
  props: {
    id: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const loading = ref<boolean>(false);
    const reloadMain = inject('reloadMain') as () => void;
    const editMode = !!props.id;
    const workflow = ref<IWorkflow>({
      name: '未命名项目',
      groupId: '1',
      global: {
        concurrent: false,
      },
      data: '',
    });
    onMounted(async () => {
      if (editMode) {
        try {
          loading.value = true;
          const { dslText, projectGroupId } = await fetchProjectDetail(props.id);
          const rawData = yaml.parse(dslText)['raw-data'];
          const { name, global } = yaml.parse(dslText);
          workflow.value = {
            name,
            groupId: projectGroupId,
            global: {
              concurrent: global ? global.concurrent : false,
            },
            data: rawData,
          };
        } catch (err) {
          proxy.$throw(err, proxy);
        } finally {
          loading.value = false;
        }
      }
    });
    const close = async () => {
      await router.push({ name: 'index' });
    };
    return {
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
          proxy.$success(editMode ? '编辑成功' : '新增成功');
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
