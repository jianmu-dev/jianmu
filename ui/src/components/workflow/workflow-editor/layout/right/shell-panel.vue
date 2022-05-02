<template>
  <div class="jm-workflow-editor-shell-panel">
    <div>This is shell panel</div>
    <div>{{ form.getName() }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { Shell } from '../../model/data/node/shell';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<Shell>,
      required: true,
    },
  },
  setup(props) {
    const form = ref<Shell>(props.nodeData);

    return {
      form,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-shell-panel {

}
</style>