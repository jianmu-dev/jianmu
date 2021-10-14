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
      value: 'cron: \'* 5/* * * * ? *\'\n' +
        '\n' +
        'event:\n' +
        '  push_event:\n' +
        '    branch: ${branch_name} # dev, master\n' +
        '  tag_event:\n' +
        '    tag: ${branch_name} # tag_name\n' +
        '\n' +
        'param:\n' +
        '  branch_name: master\n' +
        '  git_site: gitee.com\n' +
        '\n' +
        'workflow:\n' +
        '  name: 测试流程1\n' +
        '  ref: flow_1\n' +
        '  description: 测试流程1的描述1\n' +
        '  Start_1:\n' +
        '    type: start\n' +
        '    targets:\n' +
        '      - Git_1\n' +
        '  Git_1:\n' +
        '    type: git_clone0.4\n' +
        '    sources:\n' +
        '      - Start_1\n' +
        '    targets:\n' +
        '      - Build_1\n' +
        '    param:\n' +
        '      commit_branch: ${branch_name}\n' +
        '      remote_url: https://gitee.com/jianmu-dev/jianmu-workflow-core.git\n' +
        '      netrc_machine: ${git_site}\n' +
        '      netrc_username: ((gitee.user))\n' +
        '      netrc_password: ((gitee.pass))\n' +
        '  Build_1:\n' +
        '    type: maven13\n' +
        '    sources:\n' +
        '      - Git_1\n' +
        '    targets:\n' +
        '      - Condition_1\n' +
        '    param:\n' +
        '      cmd: mvn install\n' +
        '  Condition_1:\n' +
        '    type: condition\n' +
        '    sources:\n' +
        '      - Build_1\n' +
        '    expression: Git_1["commit_branch"] == "dev"\n' +
        '    cases:\n' +
        '      false: Notice_1\n' +
        '      true: Notice_2\n' +
        '  Notice_1:\n' +
        '    type: maven11\n' +
        '    param:\n' +
        '      text: \'"Build error, msg is: " + ${Build_1.build_error_message}\'\n' +
        '    sources:\n' +
        '      - Condition_1\n' +
        '    targets:\n' +
        '      - End_1\n' +
        '  Notice_2:\n' +
        '    type: maven12\n' +
        '    param:\n' +
        '      text: ${Build_1.build_info}\n' +
        '    sources:\n' +
        '      - Condition_1\n' +
        '    targets:\n' +
        '      - End_1\n' +
        '  End_1:\n' +
        '    type: end\n' +
        '    sources:\n' +
        '      - Notice_1\n' +
        '      - Notice_2\n',
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