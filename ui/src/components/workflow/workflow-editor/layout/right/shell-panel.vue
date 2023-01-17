<template>
  <div class="jm-workflow-editor-shell-panel">
    <jm-form :model="form" ref="formRef" label-position="top" @submit.prevent>
      <jm-form-item label="节点名称" prop="name" :rules="nodeData.getFormRules().name" class="node-name">
        <jm-input v-model="form.name" show-word-limit :maxlength="36" />
      </jm-form-item>
      <jm-form-item label="docker镜像" prop="image" :rules="nodeData.getFormRules().image" class="node-item">
        <jm-select
          ref="imageSelectRef"
          @keyup.enter="enterSelect"
          v-model="form.image"
          filterable
          allow-create
          clearable
          placeholder="请选择或输入镜像"
        >
          <jm-option v-for="item in defaultImages" :key="item.id" :label="item.imageName" :value="item.imageName" />
        </jm-select>
      </jm-form-item>
      <jm-form-item class="shell-env node-item">
        <template #label>
          环境变量
          <jm-tooltip placement="top">
            <template #content>
              <div>可以使用表达式，引用全局参数、事件参</div>
              <div>
                <span>数或上游节点的输出参数，详见</span>
                <a
                  href="https://v2.jianmu.dev/guide/expression.html"
                  target="_blank"
                  style="color: #fff; text-decoration: underline"
                  >参数章节</a
                >
              </div>
            </template>
            <i class="jm-icon-button-help"></i>
          </jm-tooltip>
        </template>
        <div class="shell-env-content">
          <shell-env
            v-for="(shell, index) in form.envs"
            :key="shell.key"
            v-model:name="shell.name"
            v-model:value="shell.value"
            :form-model-name="'envs'"
            :index="index"
            :rules="nodeData.getFormRules().envs.fields[index].fields"
            @delete="deleteShellEnv"
          />
          <div class="add-shell-env" @click="addShellEnv">
            <i class="jm-icon-button-add" />
            添加环境变量
          </div>
        </div>
      </jm-form-item>
      <jm-form-item label="脚本" class="script-container">
        <jm-input type="textarea" placeholder="请输入shell脚本" v-model="form.script" />
      </jm-form-item>
      <jm-form-item
        label="执行失败时"
        class="node-item"
        prop="failureMode"
        :rules="nodeData.getFormRules().failureMode"
        v-if="failureVisible"
      >
        <jm-radio-group v-model="form.failureMode">
          <jm-radio :label="'suspend'">挂起</jm-radio>
          <jm-radio :label="'ignore'">忽略</jm-radio>
        </jm-radio-group>
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
    // 镜像选择框元素
    const imageSelectRef = ref();
    const formRef = ref();
    const form = ref<Shell>(props.nodeData);
    const failureVisible = ref<boolean>(true);
    // 默认镜像
    const defaultImages = ref<Array<{ id: number; imageName: string }>>([
      {
        id: 0,
        imageName: 'docker.jianmuhub.com/library/alpine:3.17.0',
      },
      {
        id: 1,
        imageName: 'docker.jianmuhub.com/library/debian:buster-slim',
      },
      {
        id: 2,
        imageName: 'docker.jianmuhub.com/library/ubuntu:22.04',
      },
      {
        id: 3,
        imageName: 'docker.jianmuhub.com/library/anolisos:8.6',
      },
    ]);
    // 用户按下enter键选中镜像
    const enterSelect = (e: any) => {
      form.value.image = e.target.value;
      imageSelectRef.value.blur();
    };
    onMounted(() => emit('form-created', formRef.value));

    return {
      imageSelectRef,
      enterSelect,
      defaultImages,
      formRef,
      form,
      failureVisible,
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
  padding: 0 20px;

  .node-name {
    margin-top: 20px;
  }

  .node-item {
    padding-top: 10px;

    &:last-child {
      margin-bottom: 20px;
    }
  }

  .shell-env {
    .jm-icon-button-help::before {
      margin: 0;
    }

    .shell-env-content {
      border: 1px solid #e6ebf2;

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
