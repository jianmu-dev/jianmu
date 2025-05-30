<template>
  <div class="jm-workflow-editor-shell-panel">
    <jm-form :model="form" ref="formRef" label-position="top" @submit.prevent>
      <jm-form-item :label="$t('shellPanel.name')" prop="name" :rules="nodeData.getFormRules().name" class="node-name">
        <jm-input v-model="form.name" show-word-limit :maxlength="36" />
      </jm-form-item>
      <jm-form-item
        :label="$t('shellPanel.image')"
        prop="image"
        :rules="nodeData.getFormRules().image"
        class="node-item"
      >
        <jm-select
          ref="imageSelectRef"
          @keyup.enter="enterSelect"
          v-model="form.image"
          filterable
          allow-create
          clearable
          :placeholder="$t('shellPanel.imagePlaceholder')"
        >
          <jm-option v-for="item in defaultImages" :key="item.id" :label="item.imageName" :value="item.imageName" />
        </jm-select>
      </jm-form-item>
      <jm-form-item class="shell-env node-item">
        <template #label>
          {{ $t('shellPanel.env') }}
          <jm-tooltip placement="top">
            <template #content>
              <div>{{ $t('shellPanel.envTooltip1') }}</div>
              <div>
                <span>{{ $t('shellPanel.envTooltip2') }}</span>
                <a
                  href="https://v2.jianmu.dev/guide/expression.html"
                  target="_blank"
                  style="color: #fff; text-decoration: underline"
                  >{{ $t('shellPanel.envTooltipLink') }}</a
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
            @change="(newVal, oldVal) => changeEnvName(index, oldVal, 'envs')"
            @delete="deleteShellEnv"
          />
          <div class="add-shell-env" @click="addShellEnv">
            <i class="jm-icon-button-add" />
            {{ $t('shellPanel.addEnv') }}
          </div>
        </div>
      </jm-form-item>
      <jm-form-item :label="$t('shellPanel.script')" class="script-container">
        <jm-input type="textarea" :placeholder="$t('shellPanel.scriptPlaceholder')" v-model="form.script" />
      </jm-form-item>
      <div class="cache-item">
        <div class="cache-label">
          {{ $t('shellPanel.cache') }}
          <jm-tooltip placement="top" :append-to-body="false" :content="$t('shellPanel.cacheTooltip')">
            <i class="jm-icon-button-help"></i>
          </jm-tooltip>
        </div>
        <cache-selector
          v-for="(item, index) in form.caches"
          :key="item.key"
          :index="index"
          v-model:cache-info="cachesInfo"
          v-model:name="item.name"
          v-model:value="item.value"
          :form-model-name="'caches'"
          :rules="form.getFormRules().caches.fields[index].fields"
          @update-disable="updateDisable"
          @update-cache="updateCache"
          @change-dir="changeDir"
          @delete-selected="deleteCacheSelector"
        />
        <div class="add-select-cache-btn">
          <span class="add-link" @click="addSelector">
            <i class="jm-icon-button-add" />
            <span>{{ $t('shellPanel.addCache') }}</span>
          </span>
        </div>
      </div>
      <jm-form-item
        :label="$t('shellPanel.failureMode')"
        class="node-item"
        prop="failureMode"
        :rules="nodeData.getFormRules().failureMode"
        v-if="failureVisible"
      >
        <jm-radio-group v-model="form.failureMode">
          <jm-radio :label="'suspend'">{{ $t('shellPanel.suspend') }}</jm-radio>
          <jm-radio :label="'ignore'">{{ $t('shellPanel.ignore') }}</jm-radio>
        </jm-radio-group>
      </jm-form-item>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { Shell } from '../../model/data/node/shell';
import ShellEnv from './form/shell-env.vue';
import CacheSelector from './form/cache-selector.vue';
import { v4 as uuidv4 } from 'uuid';

export default defineComponent({
  components: { ShellEnv, CacheSelector },
  props: {
    nodeData: {
      type: Object as PropType<Shell>,
      required: true,
    },
    caches: {
      type: Array as PropType<string[]>,
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

    // 模拟缓存列表
    const caches = ref<string[]>(props.caches || []);
    // 构造需要的数据
    const cachesInfo = ref<{ name: string; disable: boolean }[]>(
      caches.value.filter(item => item).map((name: string) => ({ name, disable: false })),
    );

    onMounted(async () => {
      // 将已有的缓存禁用
      form.value.caches.forEach(item => {
        cachesInfo.value.forEach(_item => {
          if (item.name === _item.name) {
            _item.disable = true;
          }
        });
      });
    });

    const initSelect = () => {
      // 通过已选择的索引和未选择的索引进行禁用管理
      const cacheNameList = cachesInfo.value.map(({ name }) => name);
      const selectNameList = form.value.caches.map(({ name }) => name);
      const selectedIndex: any = [];
      selectNameList.forEach(item => {
        if (cacheNameList.indexOf(item) === -1) {
          return;
        }
        selectedIndex.push(cacheNameList.indexOf(item));
      });
      const totalIndex = [];
      for (let i = 0; i < cachesInfo.value.length; i++) {
        totalIndex.push(i);
      }
      const notSelect: any = [];
      cachesInfo.value.forEach((item, index) => {
        if (selectNameList.indexOf(item.name) === -1) {
          notSelect.push(index);
        }
      });
      selectedIndex.forEach((item: any) => {
        cachesInfo.value[item].disable = true;
      });
      notSelect.forEach((item: any) => {
        cachesInfo.value[item].disable = false;
      });
    };

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
      changeEnvName: (index: number, oldVal: string, formModelName: string) => {
        form.value.envs.forEach(({ name }, idx) => {
          if (index === idx || oldVal !== name) {
            return;
          }
          formRef.value.validateField(`${formModelName}.${idx}.name`);
        });
      },
      cachesInfo,
      addSelector: () => form.value.caches.push({ key: uuidv4(), name: '', value: '' }),
      // 更新选择框状态
      updateDisable: (val: string, index: number) => {
        form.value.caches[index].name = val;
        initSelect();
      },
      updateCache: (_index: number, cacheVal: string, dirVal: string) => {
        form.value.caches.forEach((item, index) => {
          if (_index === index) {
            item.name = cacheVal;
            item.value = dirVal;
          }
        });
      },
      changeDir: () => {
        form.value.caches.forEach((item, idx) => {
          formRef.value?.validateField(`caches.${idx}.value`);
        });
      },
      deleteCacheSelector: (_name: string, index: number) => {
        // 删除
        form.value.caches.splice(index, 1);
        // 还原列表
        cachesInfo.value.forEach(item => {
          if (item.name === _name) {
            item.disable = false;
          }
        });
        // 删除后校验
        form.value.caches.forEach((item, idx) => {
          formRef.value?.validateField(`caches.${idx}.value`);
        });
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

  .cache-item {
    .cache-label {
      line-height: 20px;
      margin-bottom: 16px;
      padding-top: 10px;
      color: #3f536e;
      font-size: 14px;
    }

    ::v-deep(.cache-selector) {
      margin-bottom: 20px;
    }

    .add-select-cache-btn {
      height: 24px;
      font-weight: 400;
      font-size: 14px;
      line-height: 24px;
      color: #096dd9;
      margin-bottom: 26px;

      .add-link {
        cursor: pointer;
      }
    }
  }

  .script-container {
    padding-top: 10px;
  }
}
</style>
