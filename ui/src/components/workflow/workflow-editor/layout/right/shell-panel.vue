<template>
  <div class="jm-workflow-editor-shell-panel">
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
import { Shell } from '../../model/data/node/shell';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<Shell>,
      required: true,
    },
  },
  emits: ['form-created'],
  setup(props, { emit }) {
    const formRef = ref();
    const form = ref<Shell>(props.nodeData);

    onMounted(() => emit('form-created', formRef.value));

    return {
      formRef,
      form,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-shell-panel {

}
</style>