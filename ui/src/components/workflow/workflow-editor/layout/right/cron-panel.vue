<template>
  <div class="jm-workflow-editor-cron-panel">
    <jm-form
      :model="form"
      :rules="form.getFormRules()"
      ref="formRef"
      @submit.prevent
    >
      <jm-form-item label="schedule" prop="schedule" class="schedule-item">
        <jm-input v-model="form.schedule" placeholder="请输入cron表达式"></jm-input>
      </jm-form-item>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { Cron } from '../../model/data/node/cron';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<Cron>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const formRef = ref();
    const form = ref<Cron>(props.nodeData);

    onMounted(() => emit('form-created', formRef.value));

    return {
      formRef,
      form,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-cron-panel {
  padding: 0 20px;

  .schedule-item {
    margin-top: 20px;
  }
}
</style>