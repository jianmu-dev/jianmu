<template>
  <div class="workflow-dsl-editor">
    dsl编辑器（只读模式）：
    <jm-dsl-editor :value="value" readonly/>
    dsl编辑器（编辑模式）：
    <jm-dsl-editor v-model:value="value"/>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup() {
    return {
      value: 'name: W_D_重构DSL语法\n' +
        'description: ""\n' +
        'global:\n' +
        '  concurrent: false\n' +
        'workflow:\n' +
        '  - ref: start\n' +
        '    name: 开始\n' +
        '    task: start\n' +
        '  - ref: shell0\n' +
        '    name: shell_0\n' +
        '    image: ubuntu:22.10\n' +
        '    script: sleep 10s\n' +
        '    needs:\n' +
        '      - start\n' +
        '  - ref: sleep0\n' +
        '    name: 延迟执行_0\n' +
        '    task: sleep@1.0.0\n' +
        '    input:\n' +
        '      unit: \'"s"\'\n' +
        '      interval: 5\n' +
        '    needs:\n' +
        '      - start\n' +
        '  - ref: sleep1\n' +
        '    name: 延迟执行_1\n' +
        '    task: sleep@1.0.0\n' +
        '    input:\n' +
        '      unit: \'"s"\'\n' +
        '      interval: 5\n' +
        '    needs:\n' +
        '      - shell0\n' +
        '      - sleep0\n' +
        '  - ref: shell1\n' +
        '    name: shell_1\n' +
        '    image: ubuntu:22.10\n' +
        '    script: sleep 10s\n' +
        '    needs:\n' +
        '      - sleep0\n' +
        '      - shell0\n' +
        '  - ref: end\n' +
        '    name: 结束\n' +
        '    task: end\n' +
        '    needs:\n' +
        '      - sleep1\n' +
        '      - shell1',
    };
  },
});
</script>

<style scoped lang="less">
.workflow-dsl-editor {
  > div {
    height: 300px;
  }
}
</style>