<template>
  <div class="cache-selector">
    <div class="selector-container">
      <jm-form-item :prop="`${formModelName}.${index}.name`" :rules="rules.name">
        <jm-select v-model="cacheVal" @change="changeCache">
          <template #empty>
            <div
              style="
                width: 334px;
                height: 156px;
                box-shadow: 0px 4px 20px rgba(0, 0, 0, 0.1);
                border-radius: 2px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 14px;
                color: #79879c;
              "
            >
              暂无缓存，请先到顶部缓存模块添加缓存
            </div>
          </template>
          <jm-option
            v-for="item in cacheList"
            :key="item.name"
            :disabled="item.disable"
            :label="item.name"
            :value="item.name"
            style="width: 334px"
          />
        </jm-select>
      </jm-form-item>
      <jm-form-item :prop="`${formModelName}.${index}.value`" :rules="rules.value">
        <jm-input
          v-model="dirVal"
          @input="updateCache"
          @change="changeDir"
          class="dir-val"
          placeholder="请输入缓存要挂载的目录,以/开头"
        />
      </jm-form-item>
    </div>
    <i class="jm-icon-button-delete" @click="deleteSelected" />
  </div>
</template>

<script lang="ts">
import { ref, defineComponent, PropType, onUpdated } from 'vue';
import { CustomRule } from '../../../model/data/common';

export default defineComponent({
  props: {
    index: {
      type: Number,
      required: true,
    },
    cacheInfo: {
      type: Array as PropType<{ name: string; disable: boolean }[]>,
    },
    name: {
      type: String,
    },
    value: {
      type: String,
    },
    rules: {
      type: Object as PropType<Record<string, CustomRule>>,
      required: true,
    },
    formModelName: {
      type: String,
      required: true,
    },
  },
  emits: ['update-disable', 'update-cache', 'delete-selected', 'change-dir'],
  setup(props, { emit }) {
    const cacheList = ref<{ name: string; disable: boolean }[]>(props.cacheInfo || []);
    const cacheVal = ref<string>(props.name || '');
    const dirVal = ref<string>(props.value || '');

    onUpdated(() => {
      if (cacheVal.value === props.name) {
        return;
      }
      cacheVal.value = props.name || '';
    });

    onUpdated(() => {
      if (dirVal.value === props.value) {
        return;
      }
      dirVal.value = props.value || '';
    });

    // 更新列表
    onUpdated(() => {
      if (cacheList.value === props.cacheInfo) {
        return;
      }
      cacheList.value = props.cacheInfo || [];
    });
    return {
      cacheList,
      changeCache: () => {
        emit('update-cache', props.index, cacheVal.value, dirVal.value);
        // 更新列表显隐
        emit('update-disable', cacheVal.value, props.index);
      },
      changeDir: () => emit('change-dir'),
      updateCache: () => emit('update-cache', props.index, cacheVal.value, dirVal.value),
      deleteSelected: () => emit('delete-selected', cacheVal.value, props.index),
      cacheVal,
      dirVal,
    };
  },
});
</script>

<style scoped lang="less">
.cache-selector {
  display: flex;
  align-items: center;

  position: relative;

  .selector-container {
    display: flex;
    align-items: flex-start;

    ::v-deep(.el-form-item) {
      margin-bottom: 0;

      .el-form-item__content {
        .el-select {
          width: 88px;

          .select-trigger {
            .el-input {
              .el-input__inner {
                border-radius: 2px 0 0 2px;
              }
            }
          }
        }

        .dir-val {
          width: 246px;

          .el-input__inner {
            border-radius: 0 2px 2px 0;
          }
        }
      }

      .jm-icon-button-delete {
        display: inline-block;
        width: 24px;
        height: 24px;
        line-height: 24px;
        color: #526579;
        margin-left: 12px;
        text-align: center;

        &:hover {
          cursor: pointer;
          background: #eff7ff;
          color: #096dd9;
          border-radius: 4px;
        }
      }
    }
  }

  .jm-icon-button-delete {
    display: inline-block;
    width: 24px;
    height: 24px;
    line-height: 24px;
    color: #526579;
    margin-left: 12px;
    text-align: center;

    position: absolute;
    right: 0;
    top: 7px;

    &:hover {
      cursor: pointer;
      background: #eff7ff;
      color: #096dd9;
      border-radius: 4px;
    }
  }
}

.jm-workflow-editor .el-form .el-form-item {
  margin-bottom: 0;
}
</style>
