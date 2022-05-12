<template>
  <div class="pipeline">
    <jm-workflow-editor v-model="workflow" @back="close" @save="save"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref } from 'vue';
import { IWorkflow } from '@/components/workflow/workflow-editor/model/data/common';
import { useRouter } from 'vue-router';
import { save as saveProject } from '@/api/project';
import { IRootState } from '@/model';
import { useStore } from 'vuex';

export default defineComponent({
  setup(props) {
    const router = useRouter();
    const store = useStore();
    const editMode = !!props.id;
    const workflow = ref<IWorkflow>({
      name: '未命名项目',
      groupId: '1',
      global: {
        concurrent: false,
      },
      data: '',
    });
    const { proxy } = getCurrentInstance() as any;
    const rootState = store.state as IRootState;
    const close = () => {
      if (rootState.fromRoute.fullPath.startsWith('/full/project/pipeline-editor')) {
        router.push({ name: 'index' });
        return;
      }
      router.push(rootState.fromRoute.fullPath);
    };
    return {
      workflow,
      close,
      save: async (back: boolean, dsl: string) => {
        try {
          const { id } = await saveProject({ projectGroupId: workflow.value.groupId, dslText: dsl });
          proxy.$success(editMode ? '编辑成功' : '新增成功');
          if (!back) {
            return;
          }
          close();
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
