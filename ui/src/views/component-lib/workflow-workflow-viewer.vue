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
      workflow: 'cron: \'* 5/* * * * ? *\'\n' +
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
      pipeline: 'cron: \'* 5/* * * * ? *\'\n' +
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
        'pipeline:\n' +
        '  name: 建木-前端CI&CD流程\n' +
        '  ref: jianmu_ui_ci_cd\n' +
        '  description: 建木-前端CI&CD流程\n' +
        '  Git_1:\n' +
        '    type: git_clone0.3\n' +
        '    param:\n' +
        '      workspace: jianmu-ci-ui\n' +
        '      commit_branch: ${branch_name}\n' +
        '      remote_url: https://gitee.com/jianmu-dev/jianmu.git\n' +
        '      netrc_machine: ${git_site}\n' +
        '      netrc_username: ((gitee.user))\n' +
        '      netrc_password: ((gitee.pass))\n' +
        '  Build_1:\n' +
        '    type: node14.2\n' +
        '    param:\n' +
        '      workspace: jianmu-ci-ui\n' +
        '  Upload_1:\n' +
        '    type: file_upload0.3\n' +
        '    param:\n' +
        '      minio_host: http://192.168.1.24:9000\n' +
        '      minio_access_key: ((minio.access_key))\n' +
        '      minio_secret_key: ((minio.secret_key))\n' +
        '      file_source: "jianmu-ci-ui/dist"\n' +
        '  SSH_1:\n' +
        '    type: ssh0.4\n' +
        '    param:\n' +
        '      ssh_host: ethan@192.168.1.24\n' +
        '      ssh_private_key: ((ssh.private_key))\n',
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