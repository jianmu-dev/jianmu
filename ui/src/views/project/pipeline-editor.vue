<template>
  <div class="pipeline">
    <jm-workflow-editor v-model="workflow" @back="back" @save="save"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { IWorkflow } from '@/components/workflow/workflow-editor/model/data/common';

export default defineComponent({
  setup() {
    const workflow = ref<IWorkflow>();
    return {
      workflow,
      back: () => {
        console.log('This is back', workflow.value);
      },
      save: (back: boolean) => {
        console.log(workflow.value?.data);
        console.log(`This is save${back ? ' and back' : ''}`, workflow.value);
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
