<template>
  <div class="pipeline">
    <jm-workflow-editor v-model="workflow" @back="close" @save="save"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref } from 'vue';
import { IWorkflow } from '@/components/workflow/workflow-editor/model/data/common';
import { useRouter } from 'vue-router';
import { save } from '@/api/project';
import { IProjectIdVo } from '@/api/dto/project';
import { IRootState } from '@/model';
import { useStore } from 'vuex';

export default defineComponent({
  setup() {
    const router = useRouter();
    const store = useStore();
    const workflow = ref<IWorkflow>();
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
      save: (back: boolean, dsl: string) => {
        const dslText = JSON.parse(JSON.stringify(dsl)).split('raw-data')[0];
        save({ projectGroupId: '1', dslText })
          .then(async ({ id }: IProjectIdVo) => {
            if (back) {
              close();
              return;
            }
          })
          .catch((err: Error) => {
            // 关闭loading
            proxy.$throw(err, proxy);
          });
        console.log(JSON.parse(JSON.stringify(workflow.value?.data)));
        console.log(`This is save${back ? ' and back' : ''}`, dsl);
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
