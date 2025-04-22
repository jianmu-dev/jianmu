<template>
  <div class="jm-workflow-editor-cron-panel">
    <jm-form :model="form" :rules="form.getFormRules()" ref="formRef" @submit.prevent>
      <jm-form-item label="schedule" prop="schedule" class="schedule-item">
        <jm-input v-model="form.schedule" :placeholder="$t('cronPanel.placeholder')"></jm-input>
      </jm-form-item>
      <div class="param-expression">
        <div class="title">{{ $t('cronPanel.meaningTitle') }}</div>
        <p>{{ $t('cronPanel.meaningContent') }}</p>
        <div class="expression-example">
          <div class="title">{{ $t('cronPanel.exampleTitle') }}</div>
          <p>{{ $t('cronPanel.example1') }}</p>
          <p>{{ $t('cronPanel.example2') }}</p>
          <p>{{ $t('cronPanel.example3') }}</p>
          <p>{{ $t('cronPanel.example4') }}</p>
          <p>{{ $t('cronPanel.example5') }}</p>
        </div>
      </div>
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

  .param-expression {
    color: #7b8c9c;
    font-size: 12px;
    line-height: 20px;
    user-select: text;
    -moz-user-select: text;
    -webkit-user-select: text;
    -ms-user-select: text;

    .title {
      font-weight: 560;
    }

    .expression-example {
      margin-top: 15px;
    }
  }
}
</style>
