<template>
  <div class="workflow-workflow-viewer">
    流程查看器：
    <jm-workflow-viewer :dsl="workflow" readonly :trigger-type="TriggerTypeEnum.MANUAL"/>
    <jm-workflow-viewer :dsl="pipeline" :trigger-type="TriggerTypeEnum.MANUAL"/>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { TriggerTypeEnum } from '@/api/dto/enumeration';

export default defineComponent({
  setup() {
    return {
      TriggerTypeEnum,
      workflow: 'name: W_D_重构DSL语法\n' +
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
      pipeline: 'name: P_D_重构DSL语法\n' +
        'description: ""\n' +
        'global:\n' +
        '  concurrent: false\n' +
        'pipeline:\n' +
        '  - ref: shell0\n' +
        '    name: shell_0\n' +
        '    image: ubuntu:22.10\n' +
        '    script: sleep 10s\n' +
        '  - ref: sleep0\n' +
        '    name: 延迟执行_0\n' +
        '    task: sleep@1.0.0\n' +
        '    input:\n' +
        '      unit: \'"s"\'\n' +
        '      interval: 5\n' +
        '  - ref: sleep1\n' +
        '    name: 延迟执行_1\n' +
        '    task: sleep@1.0.0\n' +
        '    input:\n' +
        '      unit: \'"s"\'\n' +
        '      interval: 5\n' +
        '  - ref: shell1\n' +
        '    name: shell_1\n' +
        '    image: ubuntu:22.10\n' +
        '    script: sleep 10s',
    };
  },
});
</script>

<style scoped lang="less">
.workflow-workflow-viewer {
  > div {
    height: 500px;
    border: 1px solid #f00;
    margin: 0 auto;
  }
}
</style>