<template>
  <div class="jm-workflow-editor-shell-panel">
    <jm-form
      :model="form"
      ref="formRef"
      label-position="top"
      @submit.prevent
    >
      <jm-form-item label="节点名称" prop="name" :rules="nodeData.getFormRules().name" class="node-name">
        <jm-input v-model="form.name" clearable/>
      </jm-form-item>
      <jm-form-item label="镜像" prop="image" :rules="nodeData.getFormRules().image">
        <jm-input v-model="form.image" placeholder="请输入docker镜像"/>
      </jm-form-item>
      <jm-form-item class="shell-env">
        <template #label>
          环境变量
          <jm-tooltip placement="top">
            <template #content>
              <div>可以使用表达式，引用全局参数、事件参</div>
              <div>
                <span>数或上游节点的输出参数，详见</span>
                <a href="https://docs.jianmu.dev/guide/expression.html"
                   target="_blank"
                   style="color:#fff;text-decoration: underline;">参数章节</a>
              </div>
            </template>
            <i class="jm-icon-button-help"></i>
          </jm-tooltip>
        </template>
        <div class="shell-env-content">
          <shell-env
            v-for="(shell,index) in form.envs"
            :key="shell.key"
            v-model:name="shell.name"
            v-model:value="shell.value"
            :form-model-name="'envs'"
            :index="index"
            :rules="nodeData.getFormRules().envs.fields[index].fields"
            @delete="deleteShellEnv"
          />
          <div class="add-shell-env" @click="addShellEnv">
            <i class="jm-icon-button-add"/>
            添加环境变量
          </div>
        </div>
      </jm-form-item>
      <jm-form-item label="脚本" class="script-container">
        <jm-input type="textarea" placeholder="请输入shell脚本" v-model="form.script"/>
      </jm-form-item>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { Shell } from '../../model/data/node/shell';
import ShellEnv from './form/shell-env.vue';
import { v4 as uuidv4 } from 'uuid';

export default defineComponent({
  components: { ShellEnv },
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
      // 添加环境变量
      addShellEnv: () => {
        form.value.envs.push({ key: uuidv4(), name: '', value: '' });
      },
      deleteShellEnv: (index: number) => {
        form.value.envs.splice(index, 1);
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-shell-panel {
  color: @label-color;
  font-size: 14px;
  margin-bottom: 25px;

  .node-name {
    margin-top: 20px;
  }

  .shell-env {
    .jm-icon-button-help::before {
      margin: 0;
    }

    .shell-env-content {
      border: 1px solid #E6EBF2;

      .add-shell-env {
        padding: 14px 20px;
        color: @primary-color;
        cursor: pointer;

        .jm-icon-button-add::before {
          font-weight: 700;
        }
      }
    }
  }

  .script-container {
    padding-top: 10px;
  }
}
</style>