<template>
  <div class="jm-workflow-editor-webhook-panel">
    <jm-form
      :model="form"
      :rules="form.getFormRules()"
      ref="formRef"
      @submit.prevent
    >
      <jm-form-item label="节点名称" prop="name">
        <jm-input v-model="form.name" clearable></jm-input>
      </jm-form-item>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { Webhook } from '../../model/data/node/webhook';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<Webhook>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const formRef = ref();
    const form = ref<Webhook>(props.nodeData);

    onMounted(() => emit('form-created', formRef.value));

    return {
      formRef,
      form,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-webhook-panel {

}
</style>